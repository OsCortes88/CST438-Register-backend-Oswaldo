package com.cst438;

import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static com.cst438.TestUtils.asJsonString;
import static com.cst438.TestUtils.fromJsonString;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JunitTestStudent {
    // Is in charge of creating server responses
    @Autowired
    private MockMvc mvc;

    @Test
    @DirtiesContext
    public void updateStudent() throws Exception {
        MockHttpServletResponse response;

        StudentDTO updateStudent = new StudentDTO(3,"Ryan", "ryan@csumb.edu", 0, null);

        // Perform update
        response = mvc.perform(
                MockMvcRequestBuilders
                        .put("/student/3")
                        .content(asJsonString(updateStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify update
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        StudentDTO[] student_list = fromJsonString(response.getContentAsString(), StudentDTO[].class);
        boolean found = false;
        for (StudentDTO s : student_list) {
            if (s.name().equals(updateStudent.name()) && s.email().equals(updateStudent.email())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @DirtiesContext
    public void updateStudentExceptEmail() throws Exception {
        MockHttpServletResponse response;

        StudentDTO updateStudent = new StudentDTO(3,"Ryan", "trebold@csumb.edu", 0, null);

        // Perform update
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/student/3")
                                .content(asJsonString(updateStudent))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify update
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        StudentDTO[] student_list = fromJsonString(response.getContentAsString(), StudentDTO[].class);
        boolean found = false;
        for (StudentDTO s : student_list) {
            if (s.name().equals(updateStudent.name()) && s.email().equals(updateStudent.email())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    @DirtiesContext
    public void addStudent() throws Exception {
        MockHttpServletResponse response;

        Student studentTest = new Student();
        studentTest.setName("Bruce");
        studentTest.setEmail("ironmaiden@rock.com");

        // Simulates a successful response for adding a student
        response = mvc.perform(
                MockMvcRequestBuilders
                        .post("/student")
                        .content(asJsonString(studentTest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        int newStudentsID = fromJsonString(response.getContentAsString(), Integer.class);
        assertEquals(5, newStudentsID);

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // verify that returned data contains the added student
        StudentDTO[] student_list = fromJsonString(response.getContentAsString(), StudentDTO[].class);

        boolean found = false;
        for (StudentDTO s : student_list) {
            if (s.email().equals("ironmaiden@rock.com")) {
                found = true;
            }
        }
        assertEquals(true, found, "Added student not in updated student database.");

        // Check case where same email is entered
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/student")
                                .content(asJsonString(studentTest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(406, response.getStatus());

    }

    @Test
    @DirtiesContext
    public void deleteStudentWithEnrollment() throws Exception {
        MockHttpServletResponse response;

        // Test deleting student with existing enrollment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/student/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(400, response.getStatus());
    }

    @Test
    @DirtiesContext
    public void forceDeleteStudent() throws Exception {
        MockHttpServletResponse response;

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/student/1/?FORCE=true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        // Verify student no longer exists
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        StudentDTO[] students = fromJsonString(response.getContentAsString(), StudentDTO[].class);
        boolean found = false;
        for( StudentDTO s : students) {
            if(s.studentId() == 1) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    @DirtiesContext
    public void deleteStudentWithNoEnrollment() throws Exception{
        MockHttpServletResponse response;

        Student studentTest = new Student();
        studentTest.setName("Bruce");
        studentTest.setEmail("ironmaiden@rock.com");

        // Simulates a successful response for adding a student
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/student")
                                .content(asJsonString(studentTest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());
        int newStudentID = Integer.parseInt(response.getContentAsString());

        // Test delete on student with no enrollment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/student/" + newStudentID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        StudentDTO[] students = fromJsonString(response.getContentAsString(), StudentDTO[].class);
        boolean found = false;
        for(StudentDTO s : students) {
            if(s.studentId() == newStudentID) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    @DirtiesContext
    public void deleteNonExistingStudent() throws Exception{
        MockHttpServletResponse response;

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/student/5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());
    }

    @Test
    @DirtiesContext
    public void listStudents() throws Exception {
        MockHttpServletResponse response;

        String[] emails = {"test@csumb.edu", "dwisneski@csumb.edu", "trebold@csumb.edu", "test4@csumb.edu"};

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        System.out.println(response.getContentType());
        //List<StudentDTO> students = new ArrayList<>();
        StudentDTO[] students;
        students = fromJsonString(response.getContentAsString(), StudentDTO[].class);

        assertEquals(200, response.getStatus());

        for(int i = 0; i < students.length; i++) {
            assertEquals(students[i].email(), (emails[i]));
        }

    }

}
