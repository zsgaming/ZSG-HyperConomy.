package regalowl.hyperconomy;


import java.util.Iterator;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

public class ItemDisplay {
	
	private HyperConomy hc;
	
	private String key;
	private Item item;
	private Location location;
	private String name;
	private double x;
	private double y;
	private double z;
	private String w;
	private int entityId;
	private boolean active;
	
	ItemDisplay(String key, Location location, String name) {
		this.hc = HyperConomy.hc;
		this.location = location;
		this.active = false;
		HyperEconomy he = hc.getEconomyManager().getEconomy("default");
		this.x = this.location.getX();
		this.y = this.location.getY();
		this.z = this.location.getZ();
		this.w = this.location.getWorld().getName();
		this.name = he.fixName(name);
		this.key = key;
	}
	
	ItemDisplay(Location location, String name) {
		this.hc = HyperConomy.hc;
		this.location = location;
		this.active = false;
		HyperEconomy he = hc.getEconomyManager().getEconomy("default");
		this.x = this.location.getX();
		this.y = this.location.getY();
		this.z = this.location.getZ();
		this.w = this.location.getWorld().getName();
		this.name = he.fixName(name);
		storeDisplay();
	}
	
	public boolean isActive() {
		return active;
	}
	
	public String getKey() {
		return key;
	}
	
	public Item getItem() {
		return item;	
	}
	
	public Block getBaseBlock() {
		int x = (int) Math.floor(this.x);
		int y = (int) Math.floor(this.y - 1);
		int z = (int) Math.floor(this.z);
		return getWorld().getBlockAt(x, y, z);
	}
	
	public Block getItemBlock() {
		int x = (int) Math.floor(this.x);
		int y = (int) Math.floor(this.y - 1);
		int z = (int) Math.floor(this.z);
		return getWorld().getBlockAt(x, y+1, z);
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Chunk getChunk() {
		return location.getChunk();
	}
	
	public String getName() {
		return name;
	}
	
	public double getX() {
		return location.getX();
	}
	
	public double getY() {
		return location.getY();
	}
	
	public double getZ() {
		return location.getZ();
	}
	
	public World getWorld() {
		return location.getWorld();
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public void makeDisplay() {
		if (!location.getChunk().isLoaded()) {return;}
		HyperEconomy he = hc.getEconomyManager().getEconomy("default");
		Location l = new Location(getWorld(), x, y + 1, z);
		ItemStack dropstack = he.getHyperItem(name).getItemStack();
		dropstack.setDurability((short) he.getHyperItem(name).getDurability());
		this.item = getWorld().dropItem(l, dropstack);
		this.entityId = item.getEntityId();
		item.setVelocity(new Vector(0, 0, 0));
		item.setMetadata("HyperConomy", new FixedMetadataValue(hc, "item_display"));
		active = true;
	}
	
	public void refresh() {
		removeItem();
		makeDisplay();
	}
	
	public void storeDisplay() {
		Iterator<String> it = hc.gYH().gFC("displays").getKeys(false).iterator();
		int numdisplays = 0;
		while (it.hasNext()) {
			String key = it.next().toString();
			int number = Integer.parseInt(key.substring(1, key.length()));
			if (number > numdisplays) {
				numdisplays = number;
			}
		}
		numdisplays++;
		FileConfiguration disp = hc.gYH().gFC("displays");
		key = "d" + numdisplays;
		disp.set(key + ".name", name);
		disp.set(key + ".x", x);
		disp.set(key + ".y", y);
		disp.set(key + ".z", z);
		disp.set(key + ".world", getWorld().getName());
	}
	
	public void removeItem() {
		if (item != null) {
			item.remove();
		}
		active = false;
	}
	
	public void delete() {
		FileConfiguration disp = hc.gYH().gFC("displays");
		disp.set(key, null);
		clear();
	}
	
	public void clear() {
		removeItem();
		hc = null;
		location = null;
		w = null;
		name = null;
		item = null;
		key = null;
	}
	
	
	/**
	 *
	 * @param droppedItem
	 * @return true if the item drop event shop be blocked to prevent item stacking, false if not
	 */
	public boolean blockItemDrop(Item droppedItem) {
		if (item == null) {return false;}
		HyperItemStack dropped = new HyperItemStack(droppedItem.getItemStack());
		HyperItemStack displayItem = new HyperItemStack(item.getItemStack());
		Location l = droppedItem.getLocation();
		Material dropType = droppedItem.getItemStack().getType();
		int dropda = dropped.getDamageValue();
		double dropx = l.getX();
		double dropy = l.getY();
		double dropz = l.getZ();
		World dropworld = l.getWorld();
		Material type = item.getItemStack().getType();
		int da = displayItem.getDamageValue();
		if (type == dropType) {
			if (da == dropda) {
				if (dropworld.equals(location.getWorld())) {
					if (Math.abs(dropx - location.getX()) < 10) {
						if (Math.abs(dropz - location.getZ()) < 10) {
							if (Math.abs(dropy - location.getY()) < 30) {
								return true;
							} else {
								droppedItem.setVelocity(new Vector(0,0,0));
								Block dblock = droppedItem.getLocation().getBlock();
								while (dblock.getType().equals(Material.AIR)) {
									dblock = dblock.getRelative(BlockFace.DOWN);
								}
								if (dblock.getLocation().getY() <= (location.getBlockY() + 10)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	
	public boolean blockEntityPickup(Entity entity) {
		if (entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.PIG_ZOMBIE) {
			Location el = entity.getLocation();	
			World ew = el.getWorld();
			double ex = el.getX();
			double ez = el.getZ();
			if (w.equals(ew)) {
				if (Math.abs(ex - x) < 1000) {
					if (Math.abs(ez - z) < 1000) {
						return true;
					}
				}
			}
		}
		return false;
	}
	

	public void clearNearbyItems() {
		if (item == null) {return;}
		List<Entity> nearbyEntities = item.getNearbyEntities(7, 7, 7);
		for (Entity entity : nearbyEntities) {
			if (entity instanceof Item) {
				Item nearbyItem = (Item) entity;
				boolean display = false;
				for (MetadataValue cmeta: nearbyItem.getMetadata("HyperConomy")) {
					if (cmeta.asString().equalsIgnoreCase("item_display")) {
						display = true;
						break;
					}
				}
				if (!nearbyItem.equals(item) && !display) {
					if (nearbyItem.getItemStack().getType() == item.getItemStack().getType()) {
						HyperItemStack near = new HyperItemStack(nearbyItem.getItemStack());
						HyperItemStack displayItem = new HyperItemStack(item.getItemStack());
						if (near.getDamageValue() == displayItem.getDamageValue()) {
							entity.remove();
						}
					}
				}
			}
		}
	}

}
