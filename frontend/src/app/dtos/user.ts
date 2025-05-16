import {Sex} from './sex';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  sex: Sex;
  email: string;
  housenumber?: string;
  country?: string;
  city?: string;
  street?: string;
  postalCode?: string;
}

export interface UserEdit {
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  sex: Sex;
  email: string;
  housenumber?: string;
  country?: string;
  city?: string;
  street?: string;
  postalCode?: string;
}

export function convertFromUserToEdit(user: User): UserEdit {

  return {
    firstName: user.firstName,
    lastName: user.lastName,
    dateOfBirth: user.dateOfBirth,
    sex: user.sex,
    email: user.email,
    housenumber: user.housenumber,
    country: user.country,
    city: user.city,
    street: user.street,
    postalCode: user.postalCode
  };
}
