package com.cwm.studentmanagement;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.cwm.studentmanagement.model.Courses;
import com.cwm.studentmanagement.model.Students;
import com.cwm.studentmanagement.repository.CourseRepository;
import com.cwm.studentmanagement.repository.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
class StudentmanagementApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void loginPageRendersWithCsrfToken() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("_csrf")));
	}

	@Test
	void protectedPageRedirectsToLogin() throws Exception {
		mockMvc.perform(get("/dashboard"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	void defaultAdminCanLogIn() throws Exception {
		mockMvc.perform(formLogin("/login").user("Admin").password("admin@123"))
				.andExpect(authenticated())
				.andExpect(redirectedUrl("/dashboard"));
	}

	@Test
	void wrongPasswordIsRejected() throws Exception {
		mockMvc.perform(formLogin("/login").user("Admin").password("wrong"))
				.andExpect(unauthenticated())
				.andExpect(redirectedUrl("/login?error"));
	}

	@Test
	void allMainPagesRender() throws Exception {
		String[] pages = { "/dashboard", "/students/list", "/students/new", "/course/list", "/course/new",
				"/enrollments/showEnroll", "/enrollments/enrollmentList" };
		for (String page : pages) {
			mockMvc.perform(get(page).with(user("Admin").roles("ADMIN")))
					.andExpect(status().isOk());
		}
	}

	@Test
	void fullFlow_createStudentCourse_enroll_andView() throws Exception {
		String unique = String.valueOf(System.currentTimeMillis());
		String email = "flow" + unique + "@example.com";
		String code = "C" + unique;

		mockMvc.perform(post("/students/save").with(user("Admin")).with(csrf())
				.param("firstName", "Flow").param("lastName", "Tester")
				.param("email", email).param("phoneNumber", "9876543210"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/course").with(user("Admin")).with(csrf())
				.param("courseName", "Flow Course").param("courseCode", code)
				.param("duration", "3 months").param("fee", "1500.00"))
				.andExpect(status().is3xxRedirection());

		Students student = studentRepository.findAll().stream()
				.filter(s -> email.equals(s.getEmail())).findFirst().orElseThrow();
		Courses course = courseRepository.findAll().stream()
				.filter(c -> code.equals(c.getCourseCode())).findFirst().orElseThrow();

		mockMvc.perform(post("/enrollments/enrollCourse").with(user("Admin")).with(csrf())
				.param("studentId", String.valueOf(student.getId()))
				.param("courseIds", String.valueOf(course.getId())))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/enrollments/enrollmentList?size=100").with(user("Admin")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Flow Tester")));

		mockMvc.perform(get("/enrollments/getStudentEnrollmentDetails/" + student.getId()).with(user("Admin")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Flow Course")));

		mockMvc.perform(get("/dashboard").with(user("Admin")))
				.andExpect(status().isOk());

		mockMvc.perform(get("/students/" + student.getId()).with(user("Admin")))
				.andExpect(status().isOk());
		mockMvc.perform(get("/course/" + course.getId()).with(user("Admin")))
				.andExpect(status().isOk());
	}
}
