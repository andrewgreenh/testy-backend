package testy.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
public class SessionEstablisher {

	@Autowired
	private TestClasses testClasses;

	public MockHttpSession getUserSessionWith(MockMvc mock) throws Exception {
		return (MockHttpSession) mock
		        .perform(
		                post("/login").param("username", testClasses.user.getAccountName()).param(
		                        "password", testClasses.userPassword)).andExpect(status().isOk())
		        .andReturn().getRequest().getSession();
	}

	public MockHttpSession getAdminSessionWith(MockMvc mock) throws Exception {
		return (MockHttpSession) mock
		        .perform(
		                post("/login").param("username", testClasses.admin.getAccountName()).param(
		                        "password", testClasses.adminPassword)).andExpect(status().isOk())
		        .andReturn().getRequest().getSession();
	}

}
