"use client";

import Chip from "@mui/material/Chip";
import IconButton from "@mui/material/IconButton";
import Stack from "@mui/material/Stack";
import AddIcon from "@mui/icons-material/Add";
import { FormEvent, useState } from "react";
import { APIs } from "./apis";

export default function UserRolesManagement() {
  const [roles, setRoles] = useState([] as string[]);
  const [isUserUnkown, setUserUnknown] = useState(false);
  const [email, setEmail] = useState("");
  const [roleToAdd, setRoleToAdd] = useState("");

  function onEmailChanged(event: FormEvent<HTMLInputElement>) {
    const inputValue = event.currentTarget.value;
    setEmail(inputValue);
    APIs.users
      .getRoles(inputValue)
      .then((response) => {
        setRoles(response.data || []);
        setUserUnknown(false);
      })
      .catch(() => {
        setRoles([]);
        setUserUnknown(true);
      });
  }

  function updateRoles(newRoles: string[]) {
    setRoles(newRoles);
    APIs.users.updateRoles(email, newRoles).catch((reason) => {
      console.log(reason);
    });
  }

  function onAddRoleClicked() {
    if (!roleToAdd) {
      return;
    }
    updateRoles([...roles.filter((r) => r !== roleToAdd), roleToAdd]);
    setRoleToAdd("");
  }

  return (
    <div>
      <div>
        <label htmlFor="email">e-mail : </label>
        <input
          type="email"
          id="email"
          name="email"
          required
          value={email}
          onChange={onEmailChanged}
        />
      </div>
      <div>
        <label htmlFor="newRole">nouveau role : </label>
        <input
          type="text"
          id="newRole"
          name="newRole"
          required
          value={roleToAdd}
          disabled={isUserUnkown}
          onChange={(event: FormEvent<HTMLInputElement>) =>
            setRoleToAdd(event.currentTarget.value)
          }
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              onAddRoleClicked();
            }
          }}
        />
        <IconButton
          color="primary"
          aria-label="add role"
          type="submit"
          disabled={isUserUnkown || !roleToAdd}
          onClick={onAddRoleClicked}
        >
          <AddIcon />
        </IconButton>
      </div>
      <Stack direction="row" spacing={1}>
        {roles.map((role) => (
          <Chip
            key={role}
            label={role}
            variant="outlined"
            onDelete={() => updateRoles(roles.filter((r) => r !== role))}
          />
        ))}
      </Stack>
    </div>
  );
}
