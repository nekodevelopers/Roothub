package cn.roothub.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.roothub.dao.RoleDao;
import cn.roothub.dto.PageDataBody;
import cn.roothub.entity.Role;
import cn.roothub.entity.RolePermissionRel;
import cn.roothub.exception.ApiAssert;
import cn.roothub.service.RolePermissionRelService;
import cn.roothub.service.RoleService;

/**
 * @author miansen.wang
 * @date 2019年2月27日 下午9:11:21
 */
@Service
public class RoleServiceImpl implements RoleService {
	
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private RolePermissionRelService rolePermissionRelService;

	@Override
	public Role getById(Integer id) {
		return roleDao.selectById(id);
	}

	@Override
	public PageDataBody<Role> page(Integer pageNumber, Integer pageSize) {
		List<Role> list = roleDao.selectAll((pageNumber - 1) * pageSize, pageSize);
		int countAll = this.countAll();
		return new PageDataBody<>(list, pageNumber, pageSize, countAll);
	}

	@Override
	public List<Role> getByAdminUserId(Integer adminUserId, Integer pageNumber, Integer pageSize) {
		return roleDao.selectAllByAdminUserId(adminUserId, pageNumber, pageSize);
	}

	@Override
	public void save(Role role) {
		roleDao.insert(role);
	}

	@Override
	public void update(Integer roleId,String roleName,Integer[] permissionIds) {
		Role role = roleDao.selectById(roleId);
		ApiAssert.notNull(role, "角色不存在");
		if(roleName != null && !StringUtils.isEmpty(roleName) && !role.getRoleName().equals(roleName)) {
			role.setRoleName(roleName);
			role.setUpdateDate(new Date());
			// 更新角色
			roleDao.update(role);
		}
		// 删除role与permission 的关联关系
		rolePermissionRelService.removeByRoleId(roleId);
		
		List<RolePermissionRel> list = new ArrayList<>();
		if(permissionIds != null && permissionIds.length > 0) {
			Arrays.asList(permissionIds).forEach(permissionId -> {
				RolePermissionRel rolePermissionRel = new RolePermissionRel();
				rolePermissionRel.setRoleId(roleId);
				rolePermissionRel.setPermissionId(permissionId);
				rolePermissionRel.setCreateDate(new Date());
				rolePermissionRel.setUpdateDate(new Date());
				list.add(rolePermissionRel);
			});
		}
		// 重新建立关联关系
		rolePermissionRelService.saveBatch(list);
	}

	@Override
	public void removeById(Integer id) {
		roleDao.deleteById(id);
	}

	@Override
	public int countAll() {
		return roleDao.countAll();
	}
	
}
