import { Component } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterModule, Router } from '@angular/router';
import { Apollo } from 'apollo-angular';
import { gql } from '@apollo/client/core';
import { HttpHeaders } from '@angular/common/http';

interface Booking {
  bookingId: number;
  destination: string;
  rate: number;
  bookingDate: string;
  numberOfPeople: number;
  createdAt: string;
  status: string;
}

@Component({
  selector: 'app-user-profile-destination',
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, RouterModule],
  templateUrl: './user-profile-destination.component.html',
  styleUrl: './user-profile-destination.component.css'
})
export class UserProfileDestinationComponent {
  bookings: Booking[] = [];
  token: string | null = null;

  constructor(private http: HttpClient, private apollo: Apollo) {}

  ngOnInit() {
    this.token = localStorage.getItem('authToken');
    this.fetchBookings();
  }

  fetchBookings() {
  const token = localStorage.getItem('authToken');
  if (!token) {
    console.error('No auth token found');
    return;
  }

  const GET_ALL_BOOKING_FOR_A_USER = gql`
    query GetAllBookingForAUser {
      getAllBookingForAUser {
        bookingId
        destination
        rate
        bookingDate
        numberOfPeople
        createdAt
        status
      }
    }
  `;

  this.apollo.query({
    query: GET_ALL_BOOKING_FOR_A_USER,
    context: {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      })
    },
    fetchPolicy: 'no-cache'
  }).subscribe({
    next: (res: any) => {
      console.log('Bookings fetched successfully:', res);
      this.bookings = res.data.getAllBookingForAUser;
    },
    error: (err) => {
      console.error('Error fetching bookings:', err);
    }
  });
}


  getImageForDestination(destination: string): string {
      const map: Record<string, string> = {
      'Ooty': 'assets/trips/ooty.jpg',
      'Mysore': 'assets/trips/mysuru.png',
      'Shimoga': 'assets/trips/shimoga.png',
      'Goa': 'assets/trips/goa.jpg',
      };
    return map[destination];
  }

  canCancel(status: string) {
    return status.toUpperCase() === 'PENDING';
  }

  showAlert(message: string) {
    alert(message);
  }

  confirmCancel(id: number, destination: string) {
  if (!this.token) {
    alert('You are not logged in!');
    return;
  }

  if (confirm(`Are you sure you want to cancel the booking for ${destination}?`)) {
    const UPDATE_USER_BOOKING = gql`
      mutation UpdateUserBooking($bookingId: ID!, $status: String!) {
        updateUserBooking(bookingId: $bookingId, status: $status) {
          bookingId
          destination
          status
        }
      }
    `;

    console.log("Booking ID to cancel:", id);

    this.apollo.mutate({
      mutation: UPDATE_USER_BOOKING,
      variables: {
        bookingId: id,
        status: "CANCELLED"
      },
      context: {
        headers: new HttpHeaders({
          Authorization: `Bearer ${this.token}`
        })
      }
    }).subscribe({
      next: (res: any) => {
        console.log('Booking cancelled successfully:', res);
        alert('Booking cancelled successfully!');
        this.fetchBookings();
      },
      error: (err) => {
        console.error('Error cancelling booking:', err);
        alert('Failed to cancel booking. Please try again.');
      }
    });
  }
}

}
