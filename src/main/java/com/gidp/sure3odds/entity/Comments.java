package com.gidp.sure3odds.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "sure_comments")
public class Comments {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sure_comments_seq")
	@SequenceGenerator(name = "sure_comments_seq", sequenceName = "sure_comments_seq", initialValue = 1, allocationSize = 1)
	private Long id;

	@Lob
	private String comments;

	@Temporal(TemporalType.DATE)
	private Date date;

	@Temporal(TemporalType.TIME)
	private Date time;

	@ManyToOne
	private Users userID;

	@ManyToOne
	private Games gameID;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @return the userID
	 */
	public Users getUserID() {
		return userID;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(Users userID) {
		this.userID = userID;
	}

	/**
	 * @return the gameID
	 */
	public Games getGameID() {
		return gameID;
	}

	/**
	 * @param gameID the gameID to set
	 */
	public void setGameID(Games gameID) {
		this.gameID = gameID;
	}

	@Override
	public String toString() {
		return "Comments [id=" + id + ", comments=" + comments + ", date=" + date + ", time=" + time + ", userID="
				+ userID + ", gameID=" + gameID + "]";
	}

	/**
	 *
	 */
	public Comments() {
		super();
	}

	/**
	 * @param id
	 * @param comments
	 * @param date
	 * @param time
	 */
	public Comments(Long id, String comments, Date date, Date time) {
		super();
		this.id = id;
		this.comments = comments;
		this.date = date;
		this.time = time;
	}

	/**
	 * @param comments
	 * @param date
	 * @param time
	 */
	public Comments(String comments, Date date, Date time) {
		super();
		this.comments = comments;
		this.date = date;
		this.time = time;
	}

}
