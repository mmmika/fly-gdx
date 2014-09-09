package de.fau.cs.mad.fly.profile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import de.fau.cs.mad.fly.db.FlyDBManager;
import de.fau.cs.mad.fly.player.IPlane;
import de.fau.cs.mad.fly.res.PlaneUpgrade;

/**
 * Manages the different Spaceships
 * @author Sebastian
 *
 */
public class PlaneManager {
	
	private JsonReader reader = new JsonReader();
	//private List<IPlane.Head> planes;
	private Map<Integer, IPlane.Head> planes;
	private IPlane.Head chosenPlane;
	
	private static PlaneManager Instance = new PlaneManager();
	
	public static PlaneManager getInstance() {
		return Instance;
	}

	public Map<Integer, IPlane.Head> getSpaceshipList() {
		if (planes == null) {
			
			//planes = new ArrayList<IPlane.Head>();
			planes = new HashMap<Integer, IPlane.Head>();
			FileHandle dirHandle = Gdx.files.internal("spaceships/json/");
			for (FileHandle file : dirHandle.list()) {
				JsonValue json = reader.parse(file);
				IPlane.Head planeHead = new IPlane.Head();
				
				int id = json.getInt("id");
				planeHead.id = id;
				planeHead.name = json.getString("name");
				planeHead.modelRef = json.getString("modelRef");
				planeHead.levelGroupDependency = json.getInt("levelGroupDependency");
				planeHead.speed = json.getFloat("speed");
				planeHead.rollingSpeed = json.getFloat("rollingSpeed");
				planeHead.azimuthSpeed = json.getFloat("azimuthSpeed");
				planeHead.lives = json.getInt("lives");
				JsonValue rotation = json.get("rotation");
				if (rotation != null) {
					Vector3 rotationVector = new Vector3(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2));
					planeHead.rotationSpeed = rotationVector.len();
					planeHead.rotation = rotationVector.nor();
				}
				JsonValue particleOffset = json.get("particleOffset");
				if (particleOffset != null) {
					planeHead.particleOffset = new Vector3(particleOffset.getFloat(0), particleOffset.getFloat(1), particleOffset.getFloat(2));
				}
				
				planeHead.file = file;
				
				int[] upgradeTypes = json.get("upgrades").asIntArray();
				planeHead.upgradeTypes = upgradeTypes;
				
				Collection<PlaneUpgrade> upgrades = PlaneUpgradeManager.getInstance().getUpgradeList().values();
				planeHead.getUpgradesBought().clear();
				planeHead.getUpgradesEquiped().clear();
				//Map<String, Integer> upgradeMap = new HashMap<String, Integer>();
				//Map<String, Integer> equipedMap = new HashMap<String, Integer>();
				
				Map<Integer, Integer> upgradeDB =getUpgradesFromDB( id);
				Map<Integer, Integer> equipedDB = getEquipedsFromDB(id);
				
				int size = upgradeTypes.length;
				for(PlaneUpgrade upgrade : upgrades) {
					for(int i = 0; i < size; i++) {
						if(upgrade.type == upgradeTypes[i]) {
							if(upgradeDB.get(upgrade.type) != null) {
								planeHead.getUpgradesBought().put(upgrade.name, upgradeDB.get(upgrade.type));
							} else {
								planeHead.addUpgradeBought(upgrade.name, 0);
							}
							if(equipedDB.get(upgrade.type) != null) {
								planeHead.getUpgradesBought().put(upgrade.name, equipedDB.get(upgrade.type));
							} else {
								planeHead.addUpgradeEquiped(upgrade.name, 0);
							}
						}
					}
				}
				
				//planeHead.upgradesBought = upgradeMap;
				//planeHead.upgradesEquiped = equipedMap;
				
				//planes.add(spaceshipHead);
				planes.put(id, planeHead);
			}
		}
		return planes;
	}
	
	public Map<Integer, Integer> getUpgradesFromDB(int planeID){
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		String sql = "select equiped_type, count from fly_plane_Equiped where player_id=" + PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId()
				+ " and plane_id=" + planeID;
		DatabaseCursor cursor = FlyDBManager.getInstance().selectData(sql);
		if (cursor != null && cursor.getCount() > 0) {
			result.put(cursor.getInt(0), cursor.getInt(1));
			cursor.close();
		}
		return result;
	}
	
	public Map<Integer, Integer> getEquipedsFromDB(int planeID){
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		String sql = "select update_type, count from fly_plane_upgrade where player_id=" + PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId()
				+ " and plane_id=" + planeID;
		DatabaseCursor cursor = FlyDBManager.getInstance().selectData(sql);
		if (cursor != null && cursor.getCount() > 0) {
			result.put(cursor.getInt(0), cursor.getInt(1));
			cursor.close();
		}
		return result;
	}
	
	public void updateEquiped( int planeID, int type, int newValue){
		int playerId = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId();
		String sql = "delete from fly_plane_Equiped where player_id=" + playerId + " and equiped_type=" + type + " and plane_id=" + planeID;
		String insert = "insert into fly_plane_Equiped(player_id, plane_id, equiped_type, count ) values (" + playerId + ", " + planeID + "," + type + "," + newValue + ")";
		FlyDBManager.getInstance().execSQL(sql);
		FlyDBManager.getInstance().execSQL(insert);	
	}
	
	public void updateUpdate( int planeID, int type, int newValue){
		int playerId = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId();
		String sql = "delete from fly_plane_upgrade where player_id=" + playerId + " and update_type=" + type + " and plane_id=" + planeID;
		String insert = "insert into fly_plane_upgrade(player_id, plane_id, update_type, count ) values (" + playerId  + ", " + planeID + "," + type + "," + newValue + ")";
		FlyDBManager.getInstance().execSQL(sql);
		FlyDBManager.getInstance().execSQL(insert);	
	}

	public int getUpgradeType( String name){
		Collection<PlaneUpgrade> upgrades = PlaneUpgradeManager.getInstance().getUpgradeList().values();
		for(PlaneUpgrade upgrade : upgrades) {
			if(upgrade.name.equals(name)){
				return upgrade.type;
			}
		}
		return 0;
	}
	
	public IPlane.Head getChosenPlane() {
		if (chosenPlane == null) {
			chosenPlane = getSpaceshipList().get(1);
		}
		return chosenPlane;
	}
	
	public IPlane.Head getNextPlane(int left) {
		if(chosenPlane == null) {
			chosenPlane = getSpaceshipList().get(1);
		}
		
		int chosenPlaneId = chosenPlane.id;
		
		chosenPlaneId -= left;
		if(chosenPlaneId < 0) {
			chosenPlaneId += planes.size();
		} else if(chosenPlaneId >= planes.size()) {
			chosenPlaneId -= planes.size();
		}
		
		chosenPlane = getSpaceshipList().get(chosenPlaneId);
		
		//((Fly) Gdx.app.getApplicationListener()).getGameController().getPlayer().setPlane(new Spaceship(chosenPlane));
		PlayerProfileManager.getInstance().getCurrentPlayerProfile().setPlane(chosenPlane);
		
		return chosenPlane;
	}

	public void setChosenPlane(IPlane.Head plane) {
		chosenPlane = plane;
	}
	
	public IPlane.Head upgradePlane(String upgradeName, int signum) {
		PlaneUpgrade upgrade = PlaneUpgradeManager.getInstance().getUpgrade(upgradeName);
		
		/*int size = upgrade.upgradeValues.length;
		for(int i = 0; i < size; i++) {
			plane.
		}*/
		int[] values = upgrade.upgradeValues;
		
		chosenPlane.speed += values[0] * signum;
		chosenPlane.rollingSpeed += values[1] * signum;
		chosenPlane.azimuthSpeed += values[2] * signum;
		chosenPlane.lives += values[3] * signum;
		
		int oldValue = chosenPlane.getUpgradesEquiped().get(upgradeName);
		chosenPlane.getUpgradesEquiped().put(upgradeName, oldValue + signum);
		this.updateEquiped(chosenPlane.id, this.getUpgradeType(upgradeName),  oldValue + signum);
		
		planes.put(chosenPlane.id, chosenPlane);
		
		return chosenPlane;
	}
	
	public void buyUpgradeForPlane(String upgradeName) {
		int currentUpgradeBought = chosenPlane.getUpgradesBought().get(upgradeName);
		
		PlaneUpgrade upgrade = PlaneUpgradeManager.getInstance().getUpgrade(upgradeName);
		int maxUpgrade = upgrade.timesAvailable;
		
		if(currentUpgradeBought < maxUpgrade) {
			if(PlayerProfileManager.getInstance().getCurrentPlayerProfile().addMoney(-upgrade.price)) {
				chosenPlane.getUpgradesBought().put(upgradeName, currentUpgradeBought + 1);
				this.updateUpdate(chosenPlane.id, this.getUpgradeType(upgradeName), currentUpgradeBought + 1);
			}
		}
	}
	
	public boolean upgradeCanBeBought(PlaneUpgrade upgrade) {
		int money = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getMoney();
		int currentlyBought = chosenPlane.getUpgradesBought().get(upgrade.name);
		if(currentlyBought < upgrade.timesAvailable && upgrade.price <= money) {
			return true;
		}
		return false;
	}
}