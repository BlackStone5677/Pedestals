package com.mowmaster.pedestals.item.pedestalUpgrades;

import com.mowmaster.pedestals.references.Reference;
import com.mowmaster.pedestals.tiles.PedestalTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static com.mowmaster.pedestals.pedestals.PEDESTALS_TAB;

public class ItemUpgradeBaseExpFilter extends ItemUpgradeBaseFilter {

    public ItemUpgradeBaseExpFilter(Properties builder) {super(builder.group(PEDESTALS_TAB));}

    @Override
    public Boolean canAcceptCapacity() {
        return true;
    }

    //Since Energy Transfer is as fast as possible, speed isnt needed, just capacity
    @Override
    public Boolean canAcceptOpSpeed() {
        return false;
    }

    @Override
    public int getItemEnchantability()
    {
        return 10;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }



    @Override
    public boolean canAcceptItem(World world, BlockPos posPedestal, ItemStack itemStackIn)
    {
        return false;
    }

    @Override
    public int canAcceptCount(World world, BlockPos posPedestal, ItemStack inPedestal, ItemStack itemStackIncoming)
    {
        TileEntity tile = world.getTileEntity(posPedestal);
        if(tile instanceof PedestalTileEntity)
        {
            PedestalTileEntity pedestal = (PedestalTileEntity)tile;
            return pedestal.getSlotSizeLimit();
        }
        //int stackabe = itemStackIncoming.getMaxStackSize();
        return 0;
    }

    @Override
    public int getComparatorRedstoneLevel(World worldIn, BlockPos pos)
    {
        int intItem=0;
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PedestalTileEntity) {
            PedestalTileEntity pedestal = (PedestalTileEntity) tileEntity;
            ItemStack coin = pedestal.getCoinOnPedestal();
            if(getXPStored(coin)>0)
            {
                float f = (float)getXPStored(coin)/(float)readMaxXpFromNBT(coin);
                intItem = MathHelper.floor(f*14.0F)+1;
            }
        }

        return intItem;
    }

    public void setMaxXP(ItemStack stack, int value)
    {
        writeMaxXpToNBT(stack, value);
    }

    public int getExpTransferRate(ItemStack stack)
    {
        int summonRate = 55;
        switch (getCapacityModifier(stack))
        {

            case 0:
                summonRate = 55;//5
                break;
            case 1:
                summonRate=160;//10
                break;
            case 2:
                summonRate = 315;//15
                break;
            case 3:
                summonRate = 550;//20
                break;
            case 4:
                summonRate = 910;//25
                break;
            case 5:
                summonRate=1395;//30
                break;
            default: summonRate=55;
        }

        return  summonRate;
    }

    public String getExpTransferRateString(ItemStack stack)
    {
        TranslationTextComponent fivelevels = new TranslationTextComponent(Reference.MODID + ".xpupgrade_tooltips" + ".rate_0");
        TranslationTextComponent tenlevels = new TranslationTextComponent(Reference.MODID + ".xpupgrade_tooltips" + ".rate_1");
        TranslationTextComponent fifteenlevels = new TranslationTextComponent(Reference.MODID + ".xpupgrade_tooltips" + ".rate_2");
        TranslationTextComponent twentylevels = new TranslationTextComponent(Reference.MODID + ".xpupgrade_tooltips" + ".rate_3");
        TranslationTextComponent twentyfivelevels = new TranslationTextComponent(Reference.MODID + ".xpupgrade_tooltips" + ".rate_4");
        TranslationTextComponent thirtylevels = new TranslationTextComponent(Reference.MODID + ".xpupgrade_tooltips" + ".rate_5");
        String tr = fivelevels.getString();
        switch (getExpTransferRate(stack))
        {
            case 55:
                tr = fivelevels.getString();
                break;
            case 160:
                tr = tenlevels.getString();
                break;
            case 315:
                tr = fifteenlevels.getString();
                break;
            case 550:
                tr = twentylevels.getString();
                break;
            case 910:
                tr = twentyfivelevels.getString();
                break;
            case 1395:
                tr = thirtylevels.getString();
                break;
            default: tr = fivelevels.getString();
        }

        return  tr;
    }



    public static int removeXp(PlayerEntity player, int amount) {
        //Someday consider using player.addExpierence()
        int startAmount = amount;
        while(amount > 0) {
            int barCap = player.xpBarCap();
            int barXp = (int) (barCap * player.experience);
            int removeXp = Math.min(barXp, amount);
            int newBarXp = barXp - removeXp;
            amount -= removeXp;//amount = amount-removeXp

            player.experienceTotal -= removeXp;
            if(player.experienceTotal < 0) {
                player.experienceTotal = 0;
            }
            if(newBarXp == 0 && amount > 0) {
                player.experienceLevel--;
                if(player.experienceLevel < 0) {
                    player.experienceLevel = 0;
                    player.experienceTotal = 0;
                    player.experience = 0;
                    break;
                } else {
                    player.experience = 1.0F;
                }
            } else {
                player.experience = newBarXp / (float) barCap;
            }
        }
        return startAmount - amount;
    }

    public void upgradeActionSendExp(World world, ItemStack coinMainPedestal, BlockPos posMainPedestal)
    {
        TileEntity pedestalInv = world.getTileEntity(posMainPedestal);
        if(pedestalInv instanceof PedestalTileEntity) {
            PedestalTileEntity tileMainPedestal = ((PedestalTileEntity) pedestalInv);
            //If this Pedestal has any Exp
            int xpMainPedestal = getXPStored(coinMainPedestal);
            if(xpMainPedestal>0)
            {
                //Grab the connected pedestals to send to
                if(tileMainPedestal.getNumberOfStoredLocations()>0)
                {
                    for(int i=0; i<tileMainPedestal.getNumberOfStoredLocations();i++)
                    {
                        BlockPos posStoredPedestal = tileMainPedestal.getStoredPositionAt(i);
                        //Make sure pedestal ISNOT powered and IS loaded in world
                        if(!world.isBlockPowered(posStoredPedestal) && world.isBlockLoaded(posStoredPedestal))
                        {
                            if(posStoredPedestal != posMainPedestal)
                            {
                                TileEntity storedPedestal = world.getTileEntity(posStoredPedestal);
                                if(storedPedestal instanceof PedestalTileEntity) {
                                    PedestalTileEntity tileStoredPedestal = ((PedestalTileEntity) storedPedestal);
                                    ItemStack coinStoredPedestal = tileStoredPedestal.getCoinOnPedestal();
                                    //Check if pedestal to send to can even be sent exp
                                    if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseExp)
                                    {
                                        int xpMaxStoredPedestal = ((ItemUpgradeBaseExp)coinStoredPedestal.getItem()).readMaxXpFromNBT(coinStoredPedestal);
                                        int xpStoredPedestal = getXPStored(coinStoredPedestal);
                                        //if Stored Pedestal has room for exp (will be lazy sending exp here)
                                        if(xpStoredPedestal < xpMaxStoredPedestal)
                                        {
                                            int transferRate = getExpTransferRate(coinMainPedestal);
                                            //If we have more then X levels in the pedestal we're sending from
                                            if(xpMainPedestal >= transferRate)
                                            {
                                                int xpRemainingMainPedestal = xpMainPedestal - transferRate;
                                                int xpRemainingStoredPedestal = xpStoredPedestal + transferRate;
                                                world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();
                                            }
                                            else
                                            {
                                                //If we have less then X levels, just send them all.
                                                int xpRemainingMainPedestal = 0;
                                                int xpRemainingStoredPedestal = xpStoredPedestal + xpMainPedestal;
                                                world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();
                                            }

                                            break;
                                        }
                                    }
                                    else if(coinStoredPedestal.getItem() instanceof ItemUpgradeBaseExpFilter)
                                    {
                                        int xpMaxStoredPedestal = ((ItemUpgradeBaseExpFilter)coinStoredPedestal.getItem()).readMaxXpFromNBT(coinStoredPedestal);
                                        int xpStoredPedestal = getXPStored(coinStoredPedestal);
                                        //if Stored Pedestal has room for exp (will be lazy sending exp here)
                                        if(xpStoredPedestal < xpMaxStoredPedestal)
                                        {
                                            int transferRate = getExpTransferRate(coinMainPedestal);
                                            //If we have more then X levels in the pedestal we're sending from
                                            if(xpMainPedestal >= transferRate)
                                            {
                                                int xpRemainingMainPedestal = xpMainPedestal - transferRate;
                                                int xpRemainingStoredPedestal = xpStoredPedestal + transferRate;
                                                world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();
                                            }
                                            else
                                            {
                                                //If we have less then X levels, just send them all.
                                                int xpRemainingMainPedestal = 0;
                                                int xpRemainingStoredPedestal = xpStoredPedestal + xpMainPedestal;
                                                world.playSound((PlayerEntity) null, posMainPedestal.getX(), posMainPedestal.getY(), posMainPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinMainPedestal,xpRemainingMainPedestal);
                                                tileMainPedestal.update();
                                                world.playSound((PlayerEntity) null, posStoredPedestal.getX(), posStoredPedestal.getY(), posStoredPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
                                                setXPStored(coinStoredPedestal,xpRemainingStoredPedestal);
                                                tileStoredPedestal.update();
                                            }

                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void actionOnCollideWithBlock(World world, PedestalTileEntity tilePedestal, BlockPos posPedestal, BlockState state, Entity entityIn)
    {
        if(entityIn instanceof ExperienceOrbEntity)
        {
            ItemStack coin = tilePedestal.getCoinOnPedestal();
            ExperienceOrbEntity getXPFromList = ((ExperienceOrbEntity)entityIn);
            world.playSound((PlayerEntity) null, posPedestal.getX(), posPedestal.getY(), posPedestal.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.15F, 1.0F);
            int currentlyStoredExp = getXPStored(coin);
            if(currentlyStoredExp < readMaxXpFromNBT(coin))
            {
                int value = getXPFromList.getXpValue();
                getXPFromList.remove();
                setXPStored(coin, currentlyStoredExp + value);
                tilePedestal.update();
            }
        }
    }

    public int getExpCountByLevel(int level)
    {
        int expUsed = 0;

        if(level <= 16)
        {
            expUsed = (level*level) + (6 * level);
        }
        else if(level > 16 && level <=31)
        {
            expUsed = (int)(((2.5 * (level*level)) - (40.5 * level))+360);
        }
        else if(level > 31)
        {
            expUsed = (int)(((4.5 * (level*level)) - (162.5 * level))+2220);
        }

        return expUsed;
    }

    public int getExpLevelFromCount(int value)
    {
        int level = 0;
        long maths = 0;
        int i = 0;
        int j = 0;

        if(value > 0 && value <= 352)
        {
            maths = (long)Math.sqrt(Math.addExact((long)36, Math.addExact((long)4,(long)value )));
            i = (int)(Math.round(Math.addExact((long)-6 , maths) / 2));
        }
        if(value > 352 && value <= 1507)
        {
            maths = (long)Math.sqrt(Math.subtractExact((long)164025, Math.multiplyExact((long)100,Math.subtractExact((long)3600,Math.multiplyExact((long)10,(long)value)))));

            i = (int)(Math.addExact((long)405 , maths) / 50);
        }
        if(value > 1507)
        {

            maths = (long)Math.sqrt(Math.subtractExact((long)2640625,Math.multiplyExact((long)180, Math.subtractExact((long)22200,Math.multiplyExact((long)10,(long)value)))));
            i = (int)(Math.addExact((long)1625 , maths) / 90);
        }

        return Math.abs(i);
    }

    public void setXPStored(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("xpstored",value);
        stack.setTag(compound);
    }

    public int getXPStored(ItemStack stack)
    {
        int storedxp = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            storedxp = getCompound.getInt("xpstored");
        }
        return storedxp;
    }

    public boolean hasMaxXpSet(ItemStack stack)
    {
        boolean returner = false;
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
            if(compound.contains("maxxp"))
            {
                returner = true;
            }
        }
        return returner;
    }


    public void writeMaxXpToNBT(ItemStack stack, int value)
    {
        CompoundNBT compound = new CompoundNBT();
        if(stack.hasTag())
        {
            compound = stack.getTag();
        }

        compound.putInt("maxxp",value);
        stack.setTag(compound);
    }

    public int readMaxXpFromNBT(ItemStack stack)
    {
        int maxxp = 0;
        if(stack.hasTag())
        {
            CompoundNBT getCompound = stack.getTag();
            maxxp = getCompound.getInt("maxxp");
        }
        return maxxp;
    }

    public int getExpBuffer(ItemStack stack)
    {
        return  0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRandomDisplayTick(PedestalTileEntity pedestal, int tick, BlockState stateIn, World world, BlockPos pos, Random rand)
    {
        if(!world.isBlockPowered(pos))
        {
            if(getXPStored(pedestal.getCoinOnPedestal())>0)
            {
                spawnParticleAroundPedestalBase(world,tick,pos,0.2f,0.95f,0.2f,1.0f);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TranslationTextComponent xpstored = new TranslationTextComponent(getTranslationKey() + ".tooltip_xpstored");
        xpstored.appendString(""+ getExpLevelFromCount(getXPStored(stack)) +"");
        xpstored.mergeStyle(TextFormatting.GREEN);
        tooltip.add(xpstored);
        TranslationTextComponent xpcapacity = new TranslationTextComponent(getTranslationKey() + ".tooltip_xpcapacity");
        TranslationTextComponent xpcapacitylvl = new TranslationTextComponent(getTranslationKey() + ".tooltip_xpcapacitylvl");
        xpcapacity.appendString(""+ getExpBuffer(stack) +"");
        xpcapacity.appendString(xpcapacitylvl.getString());
        xpcapacity.mergeStyle(TextFormatting.AQUA);
        tooltip.add(xpcapacity);
    }

}
