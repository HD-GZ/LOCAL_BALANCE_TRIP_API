package live.lbtrip.admin.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import live.lbtrip.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "admins")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.name.required}")
    @Size(max = 50, message = "{validation.name.size}")
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Size(max = 255, message = "{validation.email.size}")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(max = 255, message = "{validation.password.size}")
    @Column(nullable = false)
    private String password;

    private Admin(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static Admin create(String name, String email, String encodedPassword) {
        return new Admin(name, email, encodedPassword);
    }
}
