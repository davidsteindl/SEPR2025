import {Sex} from './sex';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  sex: Sex;
  email: string;
  address?: string;
  paymentData?: string;
}

export interface UserEdit {
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  sex: Sex;
  email: string;
  address?: string;
  paymentData?: string;
}

export function convertFromUserToEdit(user: User): UserEdit {

  return {
    firstName: user.firstName,
    lastName: user.lastName,
    dateOfBirth: user.dateOfBirth,
    sex: user.sex,
    email: user.email,
    address: user.address,
    paymentData: user.paymentData,
  };
}
