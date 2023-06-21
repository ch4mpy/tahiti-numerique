package com.c4soft.openidtraining.usersapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.c4soft.openidtraining.usersapi.domain.UserRoles;
import com.c4soft.openidtraining.usersapi.domain.UserRolesRepository;

/**
 * Avoid MethodArgumentConversionNotSupportedException with mocked repos
 *
 * @author Jérôme Wacongne &lt;ch4mp#64;c4-soft.com&gt;
 */
@TestConfiguration
@AutoConfigureDataJpa
public class EnableSpringDataWebSupportTestConf {
	@Autowired
	UserRolesRepository userRolesRepo;

	@Bean
	WebMvcConfigurer configurer() {
		return new WebMvcConfigurer() {

			@Override
			public void addFormatters(FormatterRegistry registry) {
				registry.addConverter(String.class, UserRoles.class, id -> userRolesRepo.findById(id).orElse(null));
			}
		};
	}
}