import {Sex} from "./sex";

export class RegisterUser {
  constructor(
    public firstName: string,
    public lastName: string,
    public password: string,
    public confirmPassword: string,
    public dateOfBirth: string,
    public email: string,
    public termsAccepted: boolean,
    public sex: Sex,
    public isAdmin: boolean,
    public isActivated: boolean
  ) {}
}
