package com.gidp.sure3odds.entity.games;

import com.gidp.sure3odds.entity.games.Games;
import com.gidp.sure3odds.entity.users.Users;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "sure_votes")
public class Votes {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sure_votes_seq")
	@SequenceGenerator(name = "sure_votes_seq", sequenceName = "sure_votes_seq", initialValue = 1, allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name ="gameid")
	private Games gameID;

	@ManyToOne
	@JoinColumn(name ="userid")
	private Users userID;

	private int userVote;

	private int homeVote;

	private int awayVote;

	private int drawVote;

	/**
	 *
	 */
	public Votes() {
		super();
	}

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
	 * @return the userVote
	 */
	public int getUserVote() {
		return userVote;
	}

	/**
	 * @param userVote the userVote to set
	 */
	public void setUserVote(int userVote) {
		this.userVote = userVote;
	}

	/**
	 * @return the homeVote
	 */
	public int getHomeVote() {
		return homeVote;
	}

	/**
	 * @param homeVote the homeVote to set
	 */
	public void setHomeVote(int homeVote) {
		this.homeVote = homeVote;
	}

	/**
	 * @return the awayVote
	 */
	public int getAwayVote() {
		return awayVote;
	}

	/**
	 * @param awayVote the awayVote to set
	 */
	public void setAwayVote(int awayVote) {
		this.awayVote = awayVote;
	}

	/**
	 * @return the drawVote
	 */
	public int getDrawVote() {
		return drawVote;
	}

	/**
	 * @param drawVote the drawVote to set
	 */
	public void setDrawVote(int drawVote) {
		this.drawVote = drawVote;
	}

	@Override
	public String toString() {
		return "Votes [id=" + id + ", gameID=" + gameID + ", userID=" + userID + ", userVote=" + userVote
				+ ", homeVote=" + homeVote + ", awayVote=" + awayVote + ", drawVote=" + drawVote + "]";
	}

	/**
	 * @param id
	 * @param userVote
	 * @param homeVote
	 * @param awayVote
	 * @param drawVote
	 */
	public Votes(Long id, int userVote, int homeVote, int awayVote, int drawVote) {
		super();
		this.id = id;
		this.userVote = userVote;
		this.homeVote = homeVote;
		this.awayVote = awayVote;
		this.drawVote = drawVote;
	}

	/**
	 * @param userVote
	 * @param homeVote
	 * @param awayVote
	 * @param drawVote
	 */
	public Votes(int userVote, int homeVote, int awayVote, int drawVote) {
		super();
		this.userVote = userVote;
		this.homeVote = homeVote;
		this.awayVote = awayVote;
		this.drawVote = drawVote;
	}

}