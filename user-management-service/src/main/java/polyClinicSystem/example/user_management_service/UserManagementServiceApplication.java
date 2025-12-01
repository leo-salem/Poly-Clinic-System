package polyClinicSystem.example.user_management_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import polyClinicSystem.example.user_management_service.model.enums.Gender;
import polyClinicSystem.example.user_management_service.model.enums.Role;
import polyClinicSystem.example.user_management_service.model.user.Admin;
import polyClinicSystem.example.user_management_service.repository.UserRepository;
import polyClinicSystem.example.user_management_service.config.AdminProperties;

import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class UserManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserManagementServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, AdminProperties adminProps) {

		return args -> {
			String adminEmail = adminProps.getEmail();

			Optional<Admin> existingAdmin = userRepository.findByEmail(adminEmail)
					.filter(user -> user instanceof Admin)
					.map(user -> (Admin) user);

			if (existingAdmin.isEmpty()) {
				Admin admin = new Admin();
				admin.setKeycloakID(adminProps.getKeycloakId());
				admin.setAge(adminProps.getAge());
				admin.setFirstName(adminProps.getFirstName());
				admin.setLastName(adminProps.getLastName());
				admin.setEmail(adminProps.getEmail());
				admin.setPhone(adminProps.getPhone());
				admin.setUsername(adminProps.getUsername());
				admin.setRole(Role.valueOf(adminProps.getRole()));
				admin.setGender(Gender.valueOf(adminProps.getGender()));
				admin.setAddress(adminProps.getAddress());

				userRepository.save(admin);
				System.out.println("✓ Default admin created: " + adminEmail);
			} else {
				System.out.println("✓ Admin already exists: " + adminEmail);
			}
		};
	}
}
