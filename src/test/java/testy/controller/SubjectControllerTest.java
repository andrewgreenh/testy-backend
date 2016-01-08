package testy.controller;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import testy.Application;
import testy.dataaccess.SubjectRepository;
import testy.domain.Subject;
import testy.domain.test.QuestionPool;
import testy.helper.SessionEstablisher;
import testy.helper.TestClasses;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@Transactional
@IntegrationTest
public class SubjectControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private FilterChainProxy filterChainProxy;

	@Autowired
	private WebApplicationContext webAppContext;

	@Autowired
	private SessionEstablisher sessionEstablisher;
	
	@Autowired
	TestClasses testClasses;

	private MockHttpSession userSession;
	private MockHttpSession adminSession;

	@Autowired
	private SubjectRepository subjectRepo;

	private Subject subject1;
	long subject1Id;

	ObjectMapper mapper = new ObjectMapper();
	
	@Before
	public void setUp() throws Exception {
		testClasses.initWithDb();
		
		mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).dispatchOptions(true)
		        .addFilters(filterChainProxy).build();

		userSession = sessionEstablisher.getUserSessionWith(mockMvc);
		adminSession = sessionEstablisher.getAdminSessionWith(mockMvc);
	}

	@Test
	public void GET_subjects_shouldReturnCollectionOfSubjects() throws Exception {

		// act
		MockHttpServletResponse response = mockMvc.perform(get("/subjects/").session(userSession))
		        .andExpect(status().isOk()).andReturn().getResponse();

		// assert
		try {
			mapper.readValue(response.getContentAsString(), new TypeReference<Collection<Subject>>(){});
		} catch(Exception e) {
			assertTrue("Collection of subjects should have been returned", false);
		}
	}

	@Test
	public void POST_subjects_withoutAdminPermissions_shouldReturn403() throws Exception {

		Subject newSubject = new Subject("Fach4");

		mockMvc.perform(
		        post("/subjects/").session(userSession).contentType(MediaType.APPLICATION_JSON)
		                .content(mapper.writeValueAsString(newSubject)))
		        .andExpect(status().isForbidden()).andReturn().getRequest();
	}

	@Test
	public void POST_subjects_withPermissions_shouldCreateTheNewSubject() throws Exception {

		Subject newSubject = new Subject("Fach4");

		MockHttpServletResponse response = mockMvc
		        .perform(
		                post("/subjects/").session(adminSession)
		                        .contentType(MediaType.APPLICATION_JSON)
		                        .content(mapper.writeValueAsString(newSubject)))
		        .andExpect(status().isCreated()).andReturn().getResponse();

		Subject createdSubject = mapper.readValue(response.getContentAsString(), Subject.class);
		assertTrue("Name of new subject should be equal to posted name", createdSubject.getName()
		        .equals(newSubject.getName()));
	}

	@Test
	public void GET_subjectsIdPools_shouldReturnAllQuestionPools() throws Exception {

		MockHttpServletResponse response = mockMvc
		        .perform(get("/subjects/" + subject1Id + "/pools").session(userSession))
		        .andExpect(status().isOk()).andReturn().getResponse();

		QuestionPool[] pools = mapper
		        .readValue(response.getContentAsString(), QuestionPool[].class);
		assertTrue("Ammount of questionPools not correct", pools.length == 2);
	}

	@Test
	public void POST_subjectsIdPools_withWrongPermissions_shouldReturn403() throws Exception {

		// arrange
		QuestionPool newPool = new QuestionPool("newPool");
		newPool.setPercentageToPass(200);

		mockMvc.perform(
		        post("/subjects/" + subject1.getId() + "/pools").session(userSession)
		                .contentType(MediaType.APPLICATION_JSON)
		                .content(mapper.writeValueAsString(newPool))).andExpect(
		        status().isForbidden());
	}

	@Test
	public void POST_subjectsIdPools_withCorrectPermissions_shouldReturnCreatedPool()
	        throws Exception {

		// arrange
		QuestionPool newPool = new QuestionPool("newPool");
		newPool.setPercentageToPass(200);

		// act
		MockHttpServletResponse response = mockMvc
		        .perform(
		                post("/subjects/" + subject1.getId() + "/pools").session(adminSession)
		                        .contentType(MediaType.APPLICATION_JSON)
		                        .content(mapper.writeValueAsString(newPool)))
		        .andExpect(status().isCreated()).andReturn().getResponse();

		// assert
		QuestionPool createdPool = mapper.readValue(response.getContentAsString(),
		        QuestionPool.class);
		assertTrue("Name of created pool should equal posted pool, it is '" + createdPool.getName()
		        + "' but expected was '" + newPool.getName() + "'",
		        newPool.getName().equals(createdPool.getName()));
		assertTrue("Percentage to pass should be set correctly",
		        createdPool.getPercentageToPass() == newPool.getPercentageToPass());
		assertTrue("Subject should be set correctly",
		        createdPool.getSubject().getId() == subject1Id);
	}

	@Test
	public void POST_subjectsIdPools_withCorrectPermissions_shouldCreatePool() throws Exception {

		// arrange
		QuestionPool newPool = new QuestionPool("newPool");
		newPool.setPercentageToPass(200);
		mockMvc.perform(
		        post("/subjects/" + subject1.getId() + "/pools").session(adminSession)
		                .contentType(MediaType.APPLICATION_JSON)
		                .content(mapper.writeValueAsString(newPool))).andExpect(
		        status().isCreated());

		// act
		MockHttpServletResponse response = mockMvc
		        .perform(get("/subjects/" + subject1Id + "/pools").session(userSession))
		        .andExpect(status().isOk()).andReturn().getResponse();

		// assert
		QuestionPool[] pools = mapper
		        .readValue(response.getContentAsString(), QuestionPool[].class);
		assertTrue("Ammount of questionPools not correct", pools.length == 3);
	}
}
