package at.ac.tuwien.sepr.groupphase.backend.service;

public interface TokenLinkService {

    /**
     * Method to create the One-Time-Token Link for the User to click on in the email.
     *
     * @param email the email-address of the receiving person
     * @param relativePath the Path for the function which will be triggered
     * @return the Link for the email
     */
    String createOttLink(String email, String relativePath);
}
