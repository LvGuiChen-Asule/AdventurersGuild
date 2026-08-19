package com.adventurersguild.client.screen;

import com.adventurersguild.client.AbstractGuildScreen;
import com.adventurersguild.client.ClientPartyData;
import com.adventurersguild.network.PartyDataSyncPacket;
import com.adventurersguild.network.GuildDataSyncPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** TASK-020: party overview. Operations are available via /ag party commands. */
public class PartyScreen extends AbstractGuildScreen {
    public PartyScreen(GuildDataSyncPacket data) {
        super(data, Component.translatable("ui.adventurersguild.party.title"));
    }

    @Override
    protected void rebuildWidgets() {
        addNavBar();
    }

    public void refresh(PartyDataSyncPacket packet) {
        ClientPartyData.update(packet);
        init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int x = left();
        int y = top();
        graphics.drawString(font, Component.translatable("ui.adventurersguild.party.title"),
                x + 8, y + 8, TITLE_COLOR);

        boolean hasParty = ClientPartyData.hasParty();
        String partyName = hasParty ? ClientPartyData.get().getPartyName() : data.getPartyName();
        if (partyName.isEmpty()) {
            graphics.drawString(font, Component.translatable("ui.adventurersguild.party.none"),
                    x + 12, y + 40, TEXT_COLOR);
        } else {
            graphics.drawString(font, Component.translatable("ui.adventurersguild.party.name",
                            partyName),
                    x + 12, y + 40, TITLE_COLOR);
            if (hasParty) {
                graphics.drawString(font, Component.translatable("ui.adventurersguild.party.leader",
                                ClientPartyData.get().getLeaderName()),
                        x + 12, y + 58, TEXT_COLOR);
                graphics.drawString(font, Component.translatable("ui.adventurersguild.party.members",
                                String.join(", ", ClientPartyData.get().getMembers())),
                        x + 12, y + 72, TEXT_COLOR);
                graphics.drawString(font, Component.translatable("ui.adventurersguild.party.stats",
                                ClientPartyData.get().getLevel(), ClientPartyData.get().getExperience(),
                                ClientPartyData.get().getCompletedQuests(), ClientPartyData.get().getReputation()),
                        x + 12, y + 86, DIM_TEXT_COLOR);
            }
        }
        graphics.drawString(font, Component.translatable("ui.adventurersguild.party.hint"),
                x + 12, y + 116, DIM_TEXT_COLOR);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
