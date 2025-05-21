export interface Location {
  id: number,
  name: string,
  type: string,
  country: string,
  city: string,
  street: string,
  postalCode: string
}

export interface EventLocationSearchDto {
  name?: string,
  street?: string,
  city?: string,
  country?: string,
  postalCode?: string,
  page?: number,
  size?: number;
}

