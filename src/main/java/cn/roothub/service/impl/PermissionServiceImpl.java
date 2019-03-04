package cn.roothub.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.roothub.dao.PermissionDao;
import cn.roothub.entity.Permission;
import cn.roothub.service.PermissionService;

/**
 * <p></p>
 * @author: miansen.wang
 * @date: 2019-03-01
 */
@Service
public class PermissionServiceImpl implements PermissionService {

	@Autowired
	private PermissionDao permissionDao;
	
	@Override
	public List<Permission> getBatchByRoleList(Collection<? extends Serializable> roleList) {
		return permissionDao.selectBatchByRoleList(roleList, null, null);
	}

	@Override
	public List<Permission> getAllByPid(Integer pid) {
		return permissionDao.selectAllByPid(pid, null, null);
	}

	@Override
	public void save(Permission permission) {
		permissionDao.insert(permission);
	}

	@Override
	public void update(Permission permission) {
		permissionDao.update(permission);
	}

	@Override
	public void removeById(Integer id) {
		permissionDao.deleteById(id);
	}

	@Override
	public Map<String, Object> permissionMap() {
		Map<String,Object> map = new LinkedHashMap<>();
		getAllByPid(0).forEach(role -> map.put(role.getPermissionName(), getAllByPid(role.getPermissionId())));
		return map;
	}

	@Override
	public List<Permission> getAllByRoleId(Integer roleId) {
		return permissionDao.selectAllByRoleId(roleId);
	}

}
