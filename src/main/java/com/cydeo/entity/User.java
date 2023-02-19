package com.cydeo.entity;

import com.cydeo.enums.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
//@Where(clause = "is_deleted=false")
public class User extends BaseEntity{

    private String firstName;
    private String lastName;
    private String userName;
    private String passWord;
    private boolean enabled;                    //for security
    private String phone;
    @ManyToOne                                      //many users - one role
    private Role role;
    @Enumerated(EnumType.STRING)
    private Gender gender;


}
