package edu.dvc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "volunteer_profiles")
public class VolunteerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Size(max = 500)
    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AvailabilityStatus availability = AvailabilityStatus.UNAVAILABLE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public AvailabilityStatus getAvailability() { return availability; }
    public void setAvailability(AvailabilityStatus availability) { this.availability = availability; }
}
