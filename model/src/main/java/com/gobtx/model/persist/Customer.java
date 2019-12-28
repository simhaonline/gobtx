package com.gobtx.model.persist;

import com.gobtx.model.enums.CustomerStatus;
import com.gobtx.model.enums.PhoneNumberType;

import javax.persistence.*;
import java.util.Objects;

/**
 * Created by Aaron Kuai on 2019/11/8.
 * <p>
 * Those JPA tag is use to generate the DDL why not the flay way plugin?
 * <p>
 * <p>
 * <p>
 * https://github.com/spring-projects/spring-data-examples/blob/master/jpa/multiple-datasources/src/main/java/example/springdata/jpa/multipleds/order/OrderConfig.java
 */
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customerId"}),
                @UniqueConstraint(columnNames = {"phoneNumber"}), //is the must?
                @UniqueConstraint(columnNames = {"emailAddress"}), //is the must
                @UniqueConstraint(columnNames = {"username"}),
                @UniqueConstraint(columnNames = {"nickname"})
        },
        indexes = {
                @Index(name = "Customer_customerId_index", columnList = "customerId"),
                @Index(name = "Customer_name_index", columnList = "username"),
                @Index(name = "Customer_phoneNumber_index", columnList = "phoneNumber"),
                @Index(name = "Customer_emailAddress_index", columnList = "emailAddress")
        })

public class Customer {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true)
    protected Long customerId;


    //This is primary key also
    @Column(length = 48, nullable = false)
    protected String username;

    @Column(length = 50)
    protected String nickname;

    @Column(length = 16)
    protected String phoneNumber;


    @Column(length = 64)
    protected String emailAddress;

    @Column(length = 128, nullable = false)
    protected String cryptoPassword;


    @Column
    protected String firstName;

    @Column
    protected String lastName;

    //@Enumerated(EnumType.ORDINAL)
    @Column(length = 30, nullable = false)
    protected String defaultAccountId;

    @Column
    protected long lastPasswordChangeTimestamp;

    @Column(length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    protected CustomerStatus status;

    @Column(length = 4)
    @Enumerated(EnumType.STRING)
    protected PhoneNumberType phoneNumberType;


    //if any present
    @Column
    protected long version;

    @Column
    protected long createTimestamp;

    @Column
    protected long updateTimestamp;


    public Long getId() {
        return id;
    }

    public Customer setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Customer setCustomerId(Long customerId) {
        this.customerId = customerId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Customer setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public Customer setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Customer setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Customer setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public String getCryptoPassword() {
        return cryptoPassword;
    }

    public Customer setCryptoPassword(String cryptoPassword) {
        this.cryptoPassword = cryptoPassword;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Customer setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Customer setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getDefaultAccountId() {
        return defaultAccountId;
    }

    public Customer setDefaultAccountId(String defaultAccountId) {
        this.defaultAccountId = defaultAccountId;
        return this;
    }

    public long getLastPasswordChangeTimestamp() {
        return lastPasswordChangeTimestamp;
    }

    public Customer setLastPasswordChangeTimestamp(long lastPasswordChangeTimestamp) {
        this.lastPasswordChangeTimestamp = lastPasswordChangeTimestamp;
        return this;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Customer setStatus(CustomerStatus status) {
        this.status = status;
        return this;
    }

    public PhoneNumberType getPhoneNumberType() {
        return phoneNumberType;
    }

    public Customer setPhoneNumberType(PhoneNumberType phoneNumberType) {
        this.phoneNumberType = phoneNumberType;
        return this;
    }

    public long getVersion() {
        return version;
    }

    public Customer setVersion(long version) {
        this.version = version;
        return this;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public Customer setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
        return this;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public Customer setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
