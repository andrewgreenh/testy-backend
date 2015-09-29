package testy.domain.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import testy.domain.Subject;
import testy.domain.question.Category;
import testy.domain.util.Views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class QuestionPool {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(Views.NoCircleView.class)
	private long id;
	
	@JsonView(Views.NoCircleView.class)
	private String name;
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonView(Views.NoCircleView.class)
	private Set<Category> categories = new HashSet<Category>();
	
	@JsonView(Views.NoCircleView.class)
	private int percentageToPass;
	
	@ManyToOne
	@JsonView(Views.NoCircleView.class)
	private Subject subject;

	public QuestionPool(String name) {
		this.name = name;
	}
	
	public QuestionPool() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxScoreOfConcreteTest() {
		int result = 0;
		for(Category category: categories) {
			result += category.getMaxScore();
		}
		return result;
	}

	public int getPercentageToPass() {
		return percentageToPass;
	}

	public void setPercentageToPass(int percentageToPass) {
		this.percentageToPass = percentageToPass;
	}

	public long getId() {
		return id;
	}
	
	public void addCategory(Category category) {
		if(category == null) {
			throw new NullPointerException();
		}
		categories.add(category);
	}
	
	public void removeCategory(Category category) {
		if(category == null) {
			throw new NullPointerException();
		}
		categories.remove(category);
	}
	
	public Set<Category> getCategories() {
		return Collections.unmodifiableSet(categories);
	}

	public Subject getSubject() {
		return subject;
	}

	@JsonIgnore
	public void setSubject(Subject subject) {
		this.subject = subject;
		if(!subject.getQuestionPools().contains(this)) {
			subject.addQuestionPool(this);
		}
	}
}
