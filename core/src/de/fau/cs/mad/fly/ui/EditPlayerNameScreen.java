package de.fau.cs.mad.fly.ui;

import de.fau.cs.mad.fly.profile.PlayerProfile;
import de.fau.cs.mad.fly.profile.PlayerProfileManager;

/**
 * Screen that is displayed, when pressing the button to edit the player name.
 * 
 * @author Lukas Hahmann <lukas.hahmann@gmail.com>
 * 
 */
public class EditPlayerNameScreen extends InputScreen {
    
    public EditPlayerNameScreen(BasicScreen screenToGoBack) {
        super(screenToGoBack);
    }
    
    /**
     * Save the new user name and go back to the previous screen.
     */
    protected void validInputAndGoBack() {
        String newUserName = textField.getText();
        
        PlayerProfile playerProfile = PlayerProfileManager.getInstance().getCurrentPlayerProfile();
        playerProfile.setName(newUserName);
        PlayerProfileManager.getInstance().updateIntColumn(playerProfile, "name", newUserName);
        goBackToPreviousScreen();
    }
    
}