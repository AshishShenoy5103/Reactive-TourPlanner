import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../services/BookingService';
import { Apollo, gql } from 'apollo-angular';

@Component({
  selector: 'app-admin-dashboard-user-booking-edit',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard-user-booking-edit.component.html',
  styleUrl: './admin-dashboard-user-booking-edit.component.css'
})
export class AdminDashboardUserBookingEditComponent {
  searchBookingId: number | null = null;
  booking: any = null;
  searchClicked = false;
  editMode = false;
  allowedStatuses: string[] = [];

  constructor(private http: HttpClient, private bookingService: BookingService, private apollo: Apollo) { }

  fetchBooking() {
    this.searchClicked = true;
    if (!this.searchBookingId) return;

    const token = localStorage.getItem('authToken');
    const GET_BOOKING_BY_ID = gql`
    query GetBookingById($bookingId: ID!) {
      getBookingById(bookingId: $bookingId) {
        bookingId
        userId
        destination
        rate
        bookingDate
        numberOfPeople
        createdAt
        status
      }
    }
  `;

    if (token) {
      this.apollo
        .query({
          query: GET_BOOKING_BY_ID,
          variables: { bookingId: this.searchBookingId },
          context: {
            headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
            })
          },
          fetchPolicy: 'no-cache'
        })
        .subscribe({
          next: (result: any) => {
            this.booking = result.data.getBookingById;
            this.setAllowedStatuses();
          },
          error: () => {
            this.booking = null;
          }
        });
    }
  }

  setAllowedStatuses() {
    if (!this.booking) return;
    const status = this.booking.status.toUpperCase();
    if (status === 'PENDING') {
      this.allowedStatuses = ['PENDING', 'CONFIRMED', 'CANCELLED'];
    } else if (status === 'CONFIRMED') {
      this.allowedStatuses = ['CONFIRMED', 'COMPLETED', 'CANCELLED'];
    } else {
      this.allowedStatuses = [status]; // No changes allowed
    }
  }

  onEdit() {
    this.editMode = true;
  }

  onCancel() {
    this.editMode = false;
    this.fetchBooking(); // Reset changes
  }

  onSave() {
  if (!this.booking) return;

  const token = localStorage.getItem('authToken');

  const UPDATE_USER_BOOKING = gql`
    mutation UpdateUserBooking($bookingId: ID!, $status: String!) {
      updateUserBooking(bookingId: $bookingId, status: $status) {
        bookingId
        userId
        destination
        rate
        bookingDate
        numberOfPeople
        createdAt
        status
      }
    }
  `;

  this.apollo
    .mutate({
      mutation: UPDATE_USER_BOOKING,
      variables: {
        bookingId: this.booking.bookingId,
        status: this.booking.status
      },
      context: {
        headers: new HttpHeaders({
          Authorization: `Bearer ${token}`
        })
      }
    })
    .subscribe({
      next: (result: any) => {
        this.editMode = false;
        this.bookingService.notifyBookingsChanged();
        this.fetchBooking(); // Refresh data
      },
      error: (err) => {
        console.error('Error updating booking:', err);
      }
    });
}
}
