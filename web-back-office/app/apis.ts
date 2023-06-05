import { BFFApi, Configuration as BFFConfiguration } from "@/c4-soft/bff-api";
import {
  UsersApi,
  Configuration as UsersConfiguration,
} from "@/c4-soft/users-api";

const bffApiConf = new BFFConfiguration({
  basePath: process.env.NEXT_PUBLIC_BFF_BASE_PATH,
});
const usersApiConf = new UsersConfiguration({
  basePath: process.env.NEXT_PUBLIC_BFF_BASE_PATH + "/api/v1",
});

export class APIs {
  static readonly gateway = new BFFApi(bffApiConf);
  static readonly users = new UsersApi(usersApiConf);
}