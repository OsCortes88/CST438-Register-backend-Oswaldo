package com.cst438;

import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
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

        Student updateStudent = new Student();
        updateStudent.setStudent_id(3);
        updateStudent.setName("Ryan");
        updateStudent.setEmail("ryan@csumb.edu");

        // Perform update
        response = mvc.perform(
                MockMvcRequestBuilders
                        .put("/student/update")
                        .content(asJsonString(updateStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify update
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Student[] student_list = fromJsonString(response.getContentAsString(), Student[].class);
        boolean found = false;
        for (Student s : student_list) {
            if (s.getName().equals(updateStudent.getName()) && s.getEmail().equals(updateStudent.getEmail())) {
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
                        .post("/student/add")
                        .content(asJsonString(studentTest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // I guess this means something was added
        assertEquals(200, response.getStatus());

        Student result = fromJsonString(response.getContentAsString(), Student.class);
        assertNotEquals(0, result.getStudent_id());

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // verify that returned data contains the added course
        Student[] student_list = fromJsonString(response.getContentAsString(), Student[].class);

        boolean found = false;
        for (Student s : student_list) {
            if (s.getEmail().equals("ironmaiden@rock.com")) {
                found = true;
            }
        }
        assertEquals(true, found, "Added student not in updated student database.");

        // Check case where same email is entered
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/student/add")
                                .content(asJsonString(studentTest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(406, response.getStatus());

    }

    @Test
    @DirtiesContext
    public void deleteStudent() throws Exception {
        MockHttpServletResponse response;

        // Test deleting student with existing enrollment
        response = mvc.perform(
                MockMvcRequestBuilders
                        .delete("/student/delete?id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(400, response.getStatus());

        // Test force delete on student with enrollment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/student/delete?id=1&FORCE=true")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        // Verify student no longer exists
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Student[] students = fromJsonString(response.getContentAsString(), Student[].class);
        boolean found = false;
        for( Student s : students) {
            if(s.getStudent_id() == 1) {
                found = true;
            }
        }
        assertFalse(found);

        // Test delete on student with no enrollment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/student/delete?id=4")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        for( Student s : students) {
            if(s.getStudent_id() == 1) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    @DirtiesContext
    public void listStudents() throws Exception {
        MockHttpServletResponse response;

        String[] emails = {"test@csumb.edu", "dwisneski@csumb.edu", "trebold@csumb.edu"};

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/student/list")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Student[] students = fromJsonString(response.getContentAsString(), Student[].class);

        assertEquals(200, response.getStatus());

        for(int i = 0; i < students.length; i++) {
            assertEquals(students[i].getEmail(), (emails[i]));
        }

    }

}
