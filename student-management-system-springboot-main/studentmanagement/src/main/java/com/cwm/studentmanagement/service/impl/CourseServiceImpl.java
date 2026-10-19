package com.cwm.studentmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cwm.studentmanagement.dto.CourseDTO;
import com.cwm.studentmanagement.model.Courses;
import com.cwm.studentmanagement.repository.CourseRepository;
import com.cwm.studentmanagement.service.CourseService;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

	private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);

	private final CourseRepository courseRepository;
	private final ModelMapper mapper;

	// FIXED: Explicitly public access modifier for cleaner Spring context bean lookups
	public CourseServiceImpl(CourseRepository courseRepository, ModelMapper mapper) {
		this.courseRepository = courseRepository;
		this.mapper = mapper;
	}

	@Override
	public CourseDTO createCourse(CourseDTO courseDTO) {
		log.info("Creating course with code: {}", courseDTO.getCourseCode());

		Courses courseEntity = mapper.map(courseDTO, Courses.class);
		Courses savedCourse = courseRepository.save(courseEntity);
		return mapper.map(savedCourse, CourseDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByCourseCode(String code) {
		log.info("Checking if code exists: {}", code);
		return courseRepository.existsByCourseCodeIgnoreCase(code);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CourseDTO> getCourses(int page, int size) {
		log.info("Fetching paginated active courses list from page index: {}", page);

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "id"));
		return courseRepository.findByActiveTrue(pageRequest)
				.map(course -> mapper.map(course, CourseDTO.class));
	}

	@Override
	@Transactional(readOnly = true)
	public CourseDTO getCourseById(Long id) {
		log.info("Fetching course details for ID: {}", id);

		Courses course = courseRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("No course found with ID: " + id));

		return mapper.map(course, CourseDTO.class);
	}

	@Override
	public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
		log.info("Updating course metadata for ID: {}", id);

		Courses course = courseRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("No course found with ID: " + id));

		// FIXED: Explicitly secure core identity parameters to avoid unexpected entity pointer breaks
		Long originalId = course.getId();
		java.time.LocalDateTime originalCreatedAt = course.getCreatedAt();

		mapper.map(courseDTO, course);

		// Restore protected parameters before entity flush
		course.setId(originalId);
		course.setCreatedAt(originalCreatedAt);

		Courses updated = courseRepository.save(course);
		return mapper.map(updated, CourseDTO.class);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByCourseCodeAndIdNot(String code, Long id) {
		log.info("Code verification lookup from update context -> Code: {}, ID: {}", code, id);
		return courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(code, id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CourseDTO> getAllCourses() {
		log.info("Fetching all active courses sorted by courseName for dropdown inputs.");

		// FIXED: Handles streaming explicitly via standard Sort parameters to prevent layout loading bugs
		return courseRepository.findAll(Sort.by(Direction.ASC, "courseName")).stream()
				.filter(Courses::isActive) // Double check filter step for UI components safety
				.map(course -> mapper.map(course, CourseDTO.class))
				.collect(Collectors.toList());
	}
}
