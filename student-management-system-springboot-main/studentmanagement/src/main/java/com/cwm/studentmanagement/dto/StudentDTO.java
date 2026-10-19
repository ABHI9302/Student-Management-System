package com.cwm.studentmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StudentDTO {

	private Long id;

	@NotBlank(message = "First name is required")
	@Size(max = 50, message = "First name must not exceed 50 characters") // FIXED: Added boundary limits to prevent database column overflow crash
	private String firstName;

	@NotBlank(message = "Last name is required")
	@Size(max = 50, message = "Last name must not exceed 50 characters")
	private String lastName;

	@NotBlank(message = "Email is required")
	@Email(message = "Please enter a valid email address")
	@Size(max = 100, message = "Email address must not exceed 100 characters")
	private String email;

	// FIXED: Updated regex pattern to securely allow either an empty input string OR exactly 10 digits without throwing validation crashes
	@Pattern(
			regexp = "^$|^[0-9]{10}$",
			message = "Phone number must contain exactly 10 digits"
	)
	private String phoneNumber;

	@Size(max = 500, message = "Address must be within 500 characters")
	private String address;

	// FIXED: Standardized active flag parameter state to default true values for new entries injection
	private boolean active = true;

	// Default Constructor
	public StudentDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
