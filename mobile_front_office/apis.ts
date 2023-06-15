import {BFFApi, Configuration as BFFConfiguration} from './c4-soft/bff-api';
import {
  GreetingsApi,
  Configuration as GreetingsConfiguration,
} from './c4-soft/greetings-api';
import {
  UsersApi,
  Configuration as UsersConfiguration,
} from './c4-soft/users-api';

const basePath = 'https://mobile.front-office.openid-training.c4-soft.com';

export const bffApiConf = new BFFConfiguration({
  basePath: basePath,
});
export const greetingsApiConf = new GreetingsConfiguration({
  basePath: basePath + '/bff/v1',
});
export const usersApiConf = new UsersConfiguration({
  basePath: basePath + '/bff/v1',
});

export class APIs {
  static readonly gateway = new BFFApi(bffApiConf);
  static readonly greetings = new GreetingsApi(greetingsApiConf);
  static readonly users = new UsersApi(usersApiConf);
}
