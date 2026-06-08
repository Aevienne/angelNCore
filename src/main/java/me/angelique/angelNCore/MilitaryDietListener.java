package me.angelique.angelNCore.listeners;

import me.angelique.angelNCore.events.PlayerDietChangedEvent;
import me.angelique.angelNCore.services.CompanyService;
import me.angelique.angelNCore.services.MilitaryService;
import me.angelique.angelNCore.services.ServiceRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MilitaryDietListener implements Listener {

    @EventHandler
    public void onDietChanged(PlayerDietChangedEvent event) {
        CompanyService cs = ServiceRegistry.getCompanyService();
        MilitaryService ms = ServiceRegistry.getMilitaryService();
        if (cs == null || ms == null) return;

        String companyId = cs.getCompanyForPlayer(event.getPlayerUUID());
        if (companyId == null) return;

        double buff = event.isBalanced() ? 1.15 : 1.0 + (event.getDietScore() * 0.005);
        ms.setDietBuff(companyId, Math.min(buff, 1.5));
    }
}
