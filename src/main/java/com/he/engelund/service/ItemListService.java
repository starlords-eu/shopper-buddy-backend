package com.he.engelund.service;

import com.he.engelund.entity.*;
import com.he.engelund.entity.builder.ListUserRoleBuilder;
import com.he.engelund.exception.ItemListNotFoundException;
import com.he.engelund.exception.UserNotListOwnerException;
import com.he.engelund.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ItemListService {

    private ItemListRepository itemListRepository;

    private ItemService itemService;

    private UserService userService;

    private RoleService roleService;

    private ListUserRoleService listUserRoleService;

    public List<ItemList> getItemListsOrderedByLastModified() {
        return itemListRepository.findAllByOrderByLastModifiedDesc();
    }

    public Set<ItemList> getItemLists() {
        return itemListRepository.findAllSet();
    }

    public ItemList addItemList(ItemList itemList) {
        return itemListRepository.save(itemList);
    }

    public void addItemToItemList(UUID listId, Item item) {
        var itemList = itemListRepository.getReferenceById(listId);
        addToList(itemList, item);
    }

    public void addItemToItemList(UUID listId, UUID itemId) {
        var itemList = itemListRepository.getReferenceById(listId);
        var item = itemService.getItemById(itemId);
        addToList(itemList, item);
    }

    private void addToList(ItemList itemList, Item item) {
        var items = itemList.getItems();
        items.add(item);
        itemListRepository.save(itemList);
    }

    public void shareItemList(UUID itemListId, UUID sharerId, UUID targetUserId, RoleName roleName) {
        //TODO: Consider whether more than one owner should be allowed in order to change ownership
        if(roleName == RoleName.OWNER) {
            throw new IllegalArgumentException();
        }
        var targetUser = userService.findById(targetUserId);
        var itemList = itemListRepository.findById(itemListId).orElseThrow(() -> new ItemListNotFoundException(itemListId));
        var role = roleService.findByName(roleName);

        if(userOwnsTheList(sharerId, itemList)) {
            var newListUserRole = ListUserRoleBuilder
                    .create()
                    .addUser(targetUser)
                    .addItemList(itemList)
                    .addRole(role)
                    .build();
            listUserRoleService.allocateListUserRole(newListUserRole);
        }
        else {
            throw new UserNotListOwnerException(sharerId);
        }
    }
    private boolean userOwnsTheList(UUID userId, ItemList itemList) {
        List<User> owners = itemList.getListUserRoles().stream()
                .filter(listUserRole -> listUserRole.getRole().getRoleName() == RoleName.OWNER)
                .map(ListUserRole::getUser)
                .collect(Collectors.toList());

        if (owners.isEmpty()) {
            throw new RuntimeException("No owner found for this list");
        }
        if (owners.size() > 1) {
            //TODO: Could allow more owners if needed for changing list owner functionality
            throw new RuntimeException("More than one owner found for this list");
        }
        return owners.get(0).getId().equals(userId);
    }
}
