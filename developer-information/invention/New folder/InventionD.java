package com.rs.game.player.dialogues.impl.skills.invention;

import com.rs.game.item.Item;
import com.rs.game.player.TemporaryAtributtes.Key;
import com.rs.game.player.actions.skillAction.invention.Invention;
import com.rs.game.player.actions.skillAction.invention.Invention.InventionAction;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.SkillDialogueResult;
import com.rs.game.player.dialogues.Dialogue;

public class InventionD extends Dialogue {

	private Item item;

	@Override
	public void start() {
		item = (Item) parameters[0];
		InventionAction invention = (InventionAction) parameters[1];
		SkillsDialogue.sendSkillDialogueByProduce(player, invention.getProducedItem().getId());
	}

	@Override
	public void run(int interfaceId, int componentId, int slotId) {
		SkillDialogueResult result = SkillsDialogue.getResult(player, componentId == SkillsDialogue.CONTINUE_OPTION);
		if (componentId == SkillsDialogue.CONTINUE_OPTION) {
			end();
			InventionAction invention = InventionAction.getByProduct(result.getProduce());
			if (invention == null)
				return;
			player.getActionManager().setAction(new Invention(invention, item, result.getQuantity()));
		} else if (componentId == SkillsDialogue.CHOOSE_AMOUNT_OPTION) {
			player.setCloseInterfacesEvent(null);
			SkillsDialogue.sendSkillDialogueByProduce(player, result.getProduce());
			SkillsDialogue.setProduct(player, result.getProduce());
			player.getPackets().sendIComponentInputInteger(1370, componentId, 4);
			player.getTemporaryAttributtes().put(Key.SKILLS_DIALOGUE_CHOOSE_AMOUNT, Boolean.TRUE);
		}
	}

	@Override
	public void finish() {
	}
}
