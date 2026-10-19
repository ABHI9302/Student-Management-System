package com.cwm.studentmanagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cwm.studentmanagement.dto.StudentDTO;
import com.cwm.studentmanagement.service.StudentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/students")
public class StudentController {

	private static final Logger log = LoggerFactory.getLogger(StudentController.class);

	private final StudentService studentService;

	// FIXED: Removed ModelMapperConfig direct dependency injection as it violates best practices and causes startup failures
	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping("/new")
	public String showCreateStudent(Model model) {
		log.info("Get /students/new - showing create student form.");
		model.addAttribute("studentDto", new StudentDTO());
		return "add-student";
	}

	@GetMapping("/list")
	public String listStudent(@RequestParam(defaultValue = "0") int page,
							  @RequestParam(defaultValue = "3") int size,
							  Model model,
							  @RequestParam(value="message", required = false) String message)
	{
		log.info("Get /students/list - showing paginated student catalog list.");

		Page<StudentDTO> students = studentService.getStudents(page, size);

		// FIXED: Anti-crash validation container fallback structure applied
		if (students == null) {
			students = Page.empty(PageRequest.of(page, size));
		}

		model.addAttribute("students", students);
		model.addAttribute("message", message);

		return "students"; // Perfectly maps to resources/templates/students.html
	}

	@PostMapping("/save")
	public String createStudent(@Valid @ModelAttribute("studentDto") StudentDTO studentDTO,
								BindingResult bindingResult,
								Model model,
								RedirectAttributes redirectAttributes) {

		log.info("Post /students/save - processing request to save new student record.");

		if(bindingResult.hasErrors()) {
			log.error("Post /students/save - validation errors caught, reloading input view form.");
			return "add-student";
		}

		if(studentService.existsByEmailIgnoreCase(studentDTO.getEmail())) {
			log.error("Post /students/save - unique email constraint violation caught for: {}", studentDTO.getEmail());
			bindingResult.rejectValue("email", null, "Email address must be completely unique");
			return "add-student";
		}

		studentService.createStudent(studentDTO);
		redirectAttributes.addAttribute("message", "Student added successfully!!");

		log.info("Post /students/save - entity persistent operation completed successfully.");
		return "redirect:/students/list";
	}

	@GetMapping("/{id}")
	public String getStudentById(@PathVariable Long id, Model model) {
		log.info("Get /students/{} - fetching target student detailed profile view.", id);
		StudentDTO student = studentService.getStudentById(id);
		model.addAttribute("student", student);
		return "view-student";
	}

	@GetMapping("/{id}/edit")
	public String editStudent(@PathVariable Long id, Model model) {
		log.info("Get /students/{}/edit - rendering edit profile update page.", id);
		StudentDTO student = studentService.getStudentById(id);
		model.addAttribute("studentDto", student);
		return "edit-student";
	}

	@PostMapping("/{id}/update")
	public String updateStudent(@PathVariable Long id,
								@Valid @ModelAttribute("studentDto") StudentDTO studentDTO,
								BindingResult bindingResult,
								Model model,
								RedirectAttributes redirectAttributes) {

		log.info("Post /students/{}/update - processing operational modifications update pipeline.", id);

		if(bindingResult.hasErrors()) {
			log.error("Post /students/{}/update - failed properties validation, rendering input form.");
			return "edit-student";
		}

		if(studentService.existsByEmailIgnoreCaseAndIdNot(studentDTO.getEmail(), id)) {
			log.error("Post /students/{}/update - validation conflict found, unique email parameter broken.");
			bindingResult.rejectValue("email", null, "Email address must be completely unique");
			return "edit-student";
		}

		studentService.updateStudent(id, studentDTO);
		redirectAttributes.addAttribute("message", "Student profile updated successfully!!");

		log.info("Post /students/{}/update - modification processing saved cleanly.");
		return "redirect:/students/list";
	}
}
