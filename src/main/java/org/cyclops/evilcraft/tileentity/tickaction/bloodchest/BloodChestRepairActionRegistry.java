package org.cyclops.evilcraft.tileentity.tickaction.bloodchest;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.evilcraft.api.tileentity.bloodchest.IBloodChestRepairAction;
import org.cyclops.evilcraft.api.tileentity.bloodchest.IBloodChestRepairActionRegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Registry for {@link IBloodChestRepairAction} instances.
 * @author rubensworks
 *
 */
public class BloodChestRepairActionRegistry implements IBloodChestRepairActionRegistry {

    private String[] itemBlacklist = new String[0]; // TODO: lazily read from config param

    private final List<IBloodChestRepairAction> repairActions =
            new LinkedList<IBloodChestRepairAction>();
    
    /**
     * Make a new instance.
     */
    public BloodChestRepairActionRegistry() {
    	register(new DamageableItemRepairAction());
        register(new AnvilRepairAction());
    }
    
    @Override
    public void register(IBloodChestRepairAction repairAction) {
        repairActions.add(repairAction);
    }

    @Override
    public boolean isItemValidForSlot(ItemStack itemStack) {
        if(isNotBlacklisted(itemStack)) {
            for (IBloodChestRepairAction action : repairActions) {
                if (action.isItemValidForSlot(itemStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int canRepair(ItemStack itemStack, int tick) {
        for(int i = 0; i < repairActions.size(); i++) {
            if(repairActions.get(i).canRepair(itemStack, tick)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Pair<Float, ItemStack> repair(ItemStack itemStack, Random random, int actionID, boolean doAction, boolean isBulk) {
        return repairActions.get(actionID).repair(itemStack, random, doAction, isBulk);
    }

    protected boolean isNotBlacklisted(ItemStack itemStack) {
        if(itemStack.isEmpty()) return false;
        for(String name : itemBlacklist) {
            if(itemStack.getItem().getRegistryName().toString().equals(name)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setBlacklist(String[] blacklist) {
        itemBlacklist = blacklist;
    }
    
}
