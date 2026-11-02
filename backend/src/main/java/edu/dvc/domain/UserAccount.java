package edu.dvc.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "user_accounts", indexes = @Index(name = "idx_user_role_status", columnList = "role, account_status"))
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @NotBlank @Email @Size(max = 190)
    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @NotBlank @Pattern(regexp = "^[+0-9() -]{7,20}$")
    @Column(nullable = false, length = 20)
    private String phone;

    @NotBlank
    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Size(max = 100)
    @Column(length = 100)
    private String department;

    @Size(max = 500)
    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AccountRole role;

    @Enumerated(EnumType.STRING) @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public AccountRole getRole() { return role; }
    public void setRole(AccountRole role) { this.role = role; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    public Instant getCreatedAt() { return createdAt; }
}
