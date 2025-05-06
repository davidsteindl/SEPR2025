import {Sex} from './sex';

export interface User {
  id?: number;
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  sex: Sex;
  email: string;
  address?: string;
  paymentData?: string;
}


/*export interface UserCreate {
 } */

export interface UserEdit {
  id: number;
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  sex: Sex;
  email: string;
  address?: string;
  paymentData?: string;
}

export function convertFromUserToEdit(user: User): UserEdit {
  if (!user.id) {
    throw new Error("User must have an id for editing");
  }

  return {
    id: user.id,
    firstName: user.firstName,
    lastName: user.lastName,
    dateOfBirth: user.dateOfBirth,
    sex: user.sex,
    email: user.email,
    address: user.address,
    paymentData: user.paymentData,
  };
}

