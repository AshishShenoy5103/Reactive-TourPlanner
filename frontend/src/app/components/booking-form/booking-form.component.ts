import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Apollo } from 'apollo-angular';
import { gql } from '@apollo/client/core';
import { HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-booking-form',
  imports: [FormsModule, CommonModule],
  templateUrl: './booking-form.component.html',
  styleUrl: './booking-form.component.css'
})
export class BookingFormComponent {
  today: string = new Date().toISOString().split('T')[0];

  selectedLocation: string = '';
  price: string = '';
  bookingDate: string = '';
  people: number | null = null;

  locations: string[] = ['Goa', 'Mysore', 'Shimoga', 'Ooty'];

  priceMap: Record<string, string> = {
    Goa: '18000',
    Mysore: '12000',
    Shimoga: '10000',
    Ooty: '15000',
  };

  onLocationChange() {
    this.price = this.priceMap[this.selectedLocation] || '';
  }

  constructor(private apollo: Apollo) {}

  onSubmit(form: any) {
  if (!form.valid) {
    alert('Please fill all required fields.');
    return;
  }

  const bookingData = {
    destination: this.selectedLocation,
    rate: Number(this.price),
    bookingDate: this.bookingDate,
    numberOfPeople: this.people
  };

  const token = localStorage.getItem('authToken');
  if (!token) {
    alert('User not authenticated!');
    return;
  }

  const CREATE_BOOKING = gql`
    mutation CreateBooking($bookingDTO: BookingInput!) {
      createBooking(bookingDTO: $bookingDTO) {
        destination
        rate
        bookingDate
        numberOfPeople
      }
    }
  `;

  this.apollo.mutate({
    mutation: CREATE_BOOKING,
    variables: {
      bookingDTO: bookingData
    },
    context: {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      })
    }
  }).subscribe({
    next: (res: any) => {
      console.log('Booking created successfully:', res);
      alert('Booking created successfully!');
    },
    error: (err) => {
      console.error('Error creating booking:', err);
      alert(err.message || 'Booking failed! Please try again.');
    }
  });
}

}
