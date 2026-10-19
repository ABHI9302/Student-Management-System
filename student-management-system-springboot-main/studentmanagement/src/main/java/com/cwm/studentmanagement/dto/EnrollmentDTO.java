package com.cwm.studentmanagement.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;



public class EnrollmentDTO {
	
	@NotNull(message = "Student is required")
    private Long studentId;

    @NotEmpty(message = "Select at least one course")
    private List<Long> courseIds = new ArrayList<>();

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }

}
