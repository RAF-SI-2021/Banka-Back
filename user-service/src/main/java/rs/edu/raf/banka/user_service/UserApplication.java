package rs.edu.raf.banka.user_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.edu.raf.banka.user_service.model.Permissions;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.service.UserService;

import java.util.ArrayList;
import java.util.Collection;

@SpringBootApplication()
@EnableDiscoveryClient
public class UserApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}

	@Bean
	BCryptPasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner run(UserService userService){
		return args -> {
			//Punimo Role permisijama
			Collection<String> adminPermissions = new ArrayList<>();
			adminPermissions.add(String.valueOf(Permissions.CREATE_USER));
			adminPermissions.add(String.valueOf(Permissions.DELETE_USER));
			adminPermissions.add(String.valueOf(Permissions.LIST_USERS));
			adminPermissions.add(String.valueOf(Permissions.EDIT_USER));
			adminPermissions.add(String.valueOf(Permissions.MY_EDIT));

			Collection<String> nekaPozicijaPermissions = new ArrayList<>();
			nekaPozicijaPermissions.add(String.valueOf(Permissions.MANAGE_STUFF));

			//Punimo bazu Rolama
			userService.saveRole(new Role(null, "ROLE_GL_ADMIN", adminPermissions));
			userService.saveRole(new Role(null, "ROLE_ADMIN", adminPermissions));

			//Cuvamo glavnog admina
			userService.createUserAdmin(new User("admin", "Admin123"));

			userService.createUserAdmin(new User("test", "1234", "5MYDN5OMDRTEVQPCED4F5VYKZRPZ4FRY"));

			//Setujemo Rolu adminu
			userService.setRoleToUser("admin", "ROLE_GL_ADMIN");
			//userService.setRoleToUser("arnold", "ROLE_ADMIN");

			userService.setRoleToUser("test", "ROLE_ADMIN");
		};
	}

}
