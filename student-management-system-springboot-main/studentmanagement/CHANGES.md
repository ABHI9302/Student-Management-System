# Fixes applied

1. NEW  src/main/java/com/cwm/studentmanagement/StudentmanagementApplication.java  (main class was missing)
2. EDIT repository/EnrollmentRepository.java  (e.createdAt -> e.enrolledDate; safer top-course query)
3. EDIT repository/StudentRepository.java     (enrolled list now uses inner join)
4. EDIT config/SpringConfig.java              (removed duplicate in-memory UserDetailsService, CSRF re-enabled)
5. EDIT controller/AuthController.java        (added "/" -> /dashboard redirect)
6. EDIT resources/logback-spring.xml          (C:/logs -> ./logs, modern rolling policy)
7. EDIT resources/application.properties      (env-overridable DB settings, dialect line removed)
8. REWRITE templates/enroll-course.html       (was a copy of courses.html, had no form)
9. EDIT templates/add-course.html, edit-course.html (th:errors, option value "false")
10. EDIT model/Users.java, exception/GlobalExceptionHandler.java (unused import, log format)
11. EDIT pom.xml + NEW src/test/resources/application.properties + rewritten test class (H2 in tests)
