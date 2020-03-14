package wang.miansen.roothub.config.realm;

import java.util.List;

import wang.miansen.roothub.common.dao.mapper.wrapper.query.QueryWrapper;
import wang.miansen.roothub.modules.permission.dto.PermissionDTO;
import wang.miansen.roothub.modules.role.dto.RoleDTO;
import wang.miansen.roothub.modules.user.dto.UserDTO;
import wang.miansen.roothub.modules.user.enums.UserErrorCodeEnum;
import wang.miansen.roothub.modules.user.exception.UserException;
import wang.miansen.roothub.modules.user.model.User;
import wang.miansen.roothub.modules.user.service.UserService;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p></p>
 * @author: miansen.wang
 * @date: 2019-03-01
 */
public class AccountRealm extends AuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizingRealm.class);
	
	@Autowired
	private UserService userService;
	
	/**
	 * 用户权限配置
	 * principals:身份集合，因为我们可以在 Shiro 中同时配置多个 Realm，所以身份信息可能就有多个；
	 * 因此其提供了 PrincipalCollection 用于聚合这些身份信息
	 * getPrimaryPrincipal:如果只有一个Principal，那么直接返回即可。如果有多个 Principal，因为内部使用Map存储，则随机返回一个
	 * 返回的对象是在 doGetAuthenticationInfo 里设置的认证实体信息 principal
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		// 获取 principal
		// AdminUser principal = (AdminUser)principals.getPrimaryPrincipal();
		UserDTO userDTO = (UserDTO) principals.getPrimaryPrincipal();
		// 获取用户
		// AdminUser adminUser = adminUserService.getByName(principal.getUsername());
		if(userDTO != null) {
			SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
			
			// List<Role> roles = roleService.getByAdminUserId(adminUser.getAdminUserId(), null, null);
			// 角色
			List<RoleDTO> roleDTOs = userDTO.getRoleDTOs();
			// 赋予角色
			// roles.forEach(role -> info.addRole(role.getRoleName()));
			roleDTOs.forEach(roleDTO -> info.addRole(roleDTO.getRoleName()));
			
			// List<Permission> permissions = permissionService.getBatchByRoleList(roles);
			// 赋予权限
			// permissions.forEach(permission -> info.addStringPermission(permission.getPermissionValue()));
			roleDTOs.forEach(roleDTO -> {
				List<PermissionDTO> permissionDTOs = roleDTO.getPermissionDTOs();
				permissionDTOs.forEach(permissionDTO -> info.addStringPermission(permissionDTO.getPermissionValue()));
			});
			return info;
		}
		return null;
	}

	// 组装用户信息，会被 shiro 回调，用于密码校验的
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// 1. 把 AuthenticationToken 转换为 UsernamePasswordToken 
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		
		// 2. 从 UsernamePasswordToken 中来获取 userName
		String userName = upToken.getUsername();
				
		logger.debug("用户：{} 正在登录...", userName);
		
		// 3.从数据库中查询 username 对应的用户记录
		// AdminUser adminUser = adminUserService.getByName(username);
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_name", userName);
		UserDTO userDTO = this.userService.getOne(queryWrapper);
		
		// 4.如果用户不存在，则抛出未知用户的异常
		// if(adminUser == null) throw new UnknownAccountException("用户不存在!");
		if (userDTO == null) throw new UserException(UserErrorCodeEnum.NOT_FOUND);
		
		// 5.根据用户的情况, 来构建 AuthenticationInfo 对象并返回，通常使用的实现类为: SimpleAuthenticationInfo
		
		/**
		 * 5.1 principal: 认证的实体信息. 可以是 username, 也可以是数据表对应的用户的实体类对象. 
		 * 可以通过 SecurityUtils.getSubject().getPrincipal() 拿到 principal，如果有多个，则随机返回其中的一个
		 * 也可以通过 PrincipalCollection.getPrimaryPrincipal() 拿到 principal，如果有多个，则随机返回其中的一个
		 * 也可以通过 PrincipalCollection.asSet() 拿到所有的 principal，返回的是 set 集合
		 */
		// Object principal = username;
		// AdminUser principal = new AdminUser();
		// principal.setAdminUserId(adminUser.getAdminUserId());
		// principal.setUsername(username);
		// principal.setAvatar(adminUser.getAvatar());
		
		// 5.2 credentials: 密码
		// Object credentials = adminUser.getPassword();
		Object credentials = userDTO.getPassword();
		
		// 5.3 realmName: 当前 realm 对象的 name. 调用父类的 getName() 方法即可
		String realmName = getName();
		
		// 5.4 盐值加密
		ByteSource credentialsSalt = ByteSource.Util.bytes(userName);
		
		return new SimpleAuthenticationInfo(userDTO, credentials, credentialsSalt, realmName);
	}
	
	public static void main(String[] args) {
		String hashAlgorithmName = "MD5";
		Object credentials = "123";
		Object salt = ByteSource.Util.bytes("admin");
		int hashIterations = 1024;
		Object result = new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations);
		System.out.println(result);
	}

}
