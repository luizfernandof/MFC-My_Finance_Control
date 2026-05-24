package br.com.devl.mfc.entity;

import java.util.UUID;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.enums.TransactionGroupType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "transaction_groups")
public class TransactionGroup extends BaseEntity {

	@Id
	@UuidGenerator
	@Column(updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionGroupType type;

	@Column(name = "total_installments")
	private Integer totalInstallments;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public TransactionGroupType getType() {
		return type;
	}

	public void setType(TransactionGroupType type) {
		this.type = type;
	}

	public Integer getTotalInstallments() {
		return totalInstallments;
	}

	public void setTotalInstallments(Integer totalInstallments) {
		this.totalInstallments = totalInstallments;
	}
}