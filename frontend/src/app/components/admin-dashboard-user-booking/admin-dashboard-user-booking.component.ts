import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BookingService } from '../../services/BookingService';
import { UserService } from '../../services/UserService';
import { Apollo, gql } from 'apollo-angular';

@Component({
  selector: 'app-admin-dashboard-user-booking',
  imports: [CommonModule],
  templateUrl: './admin-dashboard-user-booking.component.html',
  styleUrl: './admin-dashboard-user-booking.component.css'
})
export class AdminDashboardUserBookingComponent implements OnInit {
  bookings: any[] = [];
  selectedBooking: any = null;

  constructor(private http: HttpClient, private bookingService: BookingService, private userService: UserService, private apollo: Apollo) { }

  ngOnInit(): void {
    this.loadBookings();

    this.bookingService.bookingsChanged$.subscribe(() => {
      this.loadBookings();
    });

    this.userService.usersChanged$.subscribe(() => {
      this.loadBookings();
    });
  }

  loadBookings(): void {
    const token = localStorage.getItem('authToken');

    const GET_ALL_BOOKINGS = gql`
    query GetAllBookings {
      getAllBookings {
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
          query: GET_ALL_BOOKINGS,
          context: {
            headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
            })
          },
          fetchPolicy: 'no-cache'
        })
        .subscribe({
          next: (result: any) => {
            this.bookings = result.data.getAllBookings.map((b: any) => ({
              bookingId: b.bookingId,
              userId: b.userId,
              destination: b.destination,
              status: b.status
            }));
          },
          error: (err) => {
            console.error('Error fetching bookings:', err);
          }
        });
    }
  }


  viewBooking(bookingId: number) {
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
        variables: { bookingId },
        context: {
          headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
          })
        },
        fetchPolicy: 'no-cache'
      })
      .subscribe({
        next: (result: any) => {
          console.log(result)
          this.selectedBooking = result.data.getBookingById;
        },
        error: (err) => {
          console.error('Error fetching booking details:', err);
        }
      });
  }
}

  closeBookingModal() {
    this.selectedBooking = null;
  }
}
