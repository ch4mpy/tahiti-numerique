"use client";

import { useEffect, useState } from "react";
import Button from "@mui/material/Button";
import { APIs } from "./apis";
import UserRolesManagement from "./user-roles-management";

export class User {
  constructor(
    public name: string,
    public email: string,
    public roles: string[]
  ) {}

  get isAuthenticated(): boolean {
    return !!this.email;
  }

  static readonly ANONYMOUS = new User("", "", []);
}

export default function UserSession() {
  const [currentUser, setCurrentUser] = useState(User.ANONYMOUS);

  async function login() {
    const response = await APIs.gateway.getLoginOptions();
    if (response.data?.length < 1) {
      return;
    }
    document.location = response.data[0].loginUri;
  }

  function logout() {
    setCurrentUser(User.ANONYMOUS);
    APIs.gateway.logout().then((response) => {
      document.location = response.headers.location;
    });
  }

  useEffect(() => {
    APIs.users.getInfo().then((response) => {
      const user =
        response.status === 200
          ? new User(
              response.data.name,
              response.data.email,
              response.data.roles
            )
          : User.ANONYMOUS;
      setCurrentUser(user);
    });
  }, []);

  const content = currentUser.isAuthenticated ? (
    <div>
      <UserRolesManagement />
      <Button variant="outlined" color="primary" onClick={logout}>
        Logout
      </Button>
    </div>
  ) : (
    <Button variant="outlined" color="primary" onClick={login}>
      Login
    </Button>
  );

  return (
    <div className="z-10 w-full max-w-5xl items-center justify-between font-mono text-sm lg:flex">
      {content}
    </div>
  );
}
