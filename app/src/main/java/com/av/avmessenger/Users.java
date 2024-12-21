package com.av.avmessenger;

public class Users {

    // Fields for storing user information
    String profilepic, mail, userName, password, userId, lastMessage, status;

    // Fields for storing the serialized Kyber public key and matrix A (commented out)
    // private Rq[] publicKey;  // Store Kyber public key
    // private Rq[][] A;         // Store Kyber matrix A

    // Serialized JSON strings for public key and matrix A (to be used for encryption)
    String publicKeyJson;  // Store serialized Rq[] public key
    String matrixAJson;    // Store serialized Rq[][] matrix A

    // Default constructor for creating a Users object without parameters
    public Users() {}

    // Parameterized constructor for initializing the Users object with the provided values
    public Users(String userId, String userName, String maill, String password, String profilepic, String status, String publicKeyJson, String matrixAJson) {
        this.userId = userId; // Set user ID
        this.userName = userName; // Set username
        this.mail = maill; // Set email
        this.password = password; // Set password
        this.profilepic = profilepic; // Set profile picture
        this.status = status; // Set status message
        this.publicKeyJson = publicKeyJson; // Set the serialized public key JSON
        this.matrixAJson = matrixAJson; // Set the serialized matrix A JSON
    }

    // Getter and Setter methods for each field

    public String getProfilepic() {
        return profilepic; // Return the profile picture URL
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic; // Set the profile picture URL
    }

    public String getMail() {
        return mail; // Return the user's email address
    }

    public void setMail(String mail) {
        this.mail = mail; // Set the user's email address
    }

    public String getUserName() {
        return userName; // Return the user's username
    }

    public void setUserName(String userName) {
        this.userName = userName; // Set the user's username
    }

    public String getPassword() {
        return password; // Return the user's password
    }

    public void setPassword(String password) {
        this.password = password; // Set the user's password
    }

    public String getUserId() {
        return userId; // Return the user's ID
    }

    public void setUserId(String userId) {
        this.userId = userId; // Set the user's ID
    }

    public String getLastMessage() {
        return lastMessage; // Return the last message sent by the user
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage; // Set the last message sent by the user
    }

    public String getStatus() {
        return status; // Return the user's status message
    }

    public void setStatus(String status) {
        this.status = status; // Set the user's status message
    }

    // Getter and Setter methods for the serialized public key and matrix A (as JSON)

    public String getPublicKeyJson() {
        return publicKeyJson; // Return the serialized public key in JSON format
    }

    public void setPublicKeyJson(String publicKeyJson) {
        this.publicKeyJson = publicKeyJson; // Set the serialized public key in JSON format
    }

    public String getMatrixAJson() {
        return matrixAJson; // Return the serialized matrix A in JSON format
    }

    public void setMatrixAJson(String matrixAJson) {
        this.matrixAJson = matrixAJson; // Set the serialized matrix A in JSON format
    }
}
