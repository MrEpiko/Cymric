package me.mrepiko.cymric.elements.components;

import me.mrepiko.cymric.elements.containers.ComponentDataContainer;
import me.mrepiko.cymric.elements.containers.ConditionalDataContainer;
import me.mrepiko.cymric.elements.containers.DeferrableElementDataContainer;
import me.mrepiko.cymric.elements.containers.TimeoutableDataContainer;

public interface ForgedComponentDataContainer extends ComponentDataContainer, TimeoutableDataContainer, ConditionalDataContainer, DeferrableElementDataContainer { }
