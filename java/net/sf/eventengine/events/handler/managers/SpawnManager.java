/*
 * Copyright (C) 2015-2015 L2J EventEngine
 *
 * This file is part of L2J EventEngine.
 *
 * L2J EventEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J EventEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.eventengine.events.handler.managers;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public class SpawnManager
{
	// List of NPC in the event.
	private final Map<Integer, L2Npc> _eventNpc = new ConcurrentHashMap<>();
	
	public SpawnManager()
	{
		//
	}
	
	/**
	 * We generate a new spawn in our event and added to the list.
	 */
	public L2Npc addEventNpc(int npcId, Location loc, Team team, int instanceId)
	{
		return addEventNpc(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), team, null, false, instanceId);
	}
	
	/**
	 * We generate a new spawn in our event and added to the list.
	 */
	public L2Npc addEventNpc(int npcId, Location loc, Team team, boolean randomOffset, int instanceId)
	{
		return addEventNpc(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), team, null, randomOffset, instanceId);
	}
	
	/**
	 * We generate a new spawn in our event and added to the list.
	 */
	public L2Npc addEventNpc(int npcId, Location loc, Team team, String title, boolean randomOffset, int instanceId)
	{
		return addEventNpc(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), team, null, randomOffset, instanceId);
	}
	
	/**
	 * We generate a new spawn in our event and added to the list.
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param randomOffset -> +/- 1000
	 * @return L2Npc
	 */
	public L2Npc addEventNpc(int npcId, int x, int y, int z, int heading, Team team, String title, boolean randomOffset, int instanceId)
	{
		// We generate our npc spawn
		L2Npc npc = null;
		try
		{
			L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			if (template != null)
			{
				if (randomOffset)
				{
					x += Rnd.get(-1000, 1000);
					y += Rnd.get(-1000, 1000);
				}
				
				L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);
				spawn.setAmount(1);
				spawn.setInstanceId(instanceId);
				npc = spawn.doSpawn();// isSummonSpawn.
				npc.setTeam(team);
				
				if (title != null)
				{
					npc.setTitle(title);
				}
				
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				spawn.init();
				// animation.
				spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		// We add our npc to the list.
		_eventNpc.put(npc.getObjectId(), npc);
		
		return npc;
	}
	
	/**
	 * We get the complete list of all the NPC during the event.<br>
	 * @return Collection<PlayerHolder>
	 */
	public Collection<L2Npc> getAllEventNpc()
	{
		return _eventNpc.values();
	}
	
	/**
	 * Clear all npc generated within our event.
	 */
	public void removeAllEventNpc()
	{
		for (L2Npc npc : _eventNpc.values())
		{
			if (npc == null)
			{
				continue;
			}
			
			// We stopped the npc spawn.
			npc.getSpawn().stopRespawn();
			// Delete the npc.
			npc.deleteMe();
		}
		
		_eventNpc.clear();
	}
	
	/**
	 * Check if a NPC belongs to our event.
	 * @param npcId
	 * @return
	 */
	public boolean isNpcInEvent(L2Npc npc)
	{
		return _eventNpc.containsValue(npc.getObjectId());
	}
	
	public void removeNpc(L2Npc npc)
	{
		// We stopped the npc spawn.
		npc.getSpawn().stopRespawn();
		// Delete the npc.
		npc.deleteMe();
		
		_eventNpc.remove(npc.getObjectId());
	}
}
