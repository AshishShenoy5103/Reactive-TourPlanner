import { Component } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/UserService';
import { Apollo, gql } from 'apollo-angular';
import { BookingService } from '../../services/BookingService';

@Component({
  selector: 'app-admin-dashboard-user-edit-details',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard-user-edit-details.component.html',
  styleUrl: './admin-dashboard-user-edit-details.component.css'
})
export class AdminDashboardUserEditDetailsComponent {
  searchUserId: number | null = null;
  user: any = null;
  originalUser: any = null; // backup for cancel
  editMode = false;
  searchClicked = false;

  constructor(private http: HttpClient, private userService: UserService, private apollo: Apollo, private bookingService: BookingService) { }

  fetchUser() {
    if (!this.searchUserId) return;

    this.searchClicked = true;
    const token = localStorage.getItem('authToken');

    const GET_USER_BY_ID = gql`
    query GetUserById($userId: ID!) {
      getUserById(userId: $userId) {
        userId
        email
        userType
        createdAt
        firstName
        lastName
        aadharNumber
        city
        phoneNumber
      }
    }
  `;

    if (token) {
      this.apollo
        .query({
          query: GET_USER_BY_ID,
          variables: { userId: this.searchUserId },
          context: {
            headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
            })
          },
          fetchPolicy: 'no-cache'
        })
        .subscribe({
          next: (result: any) => {
            const data = result.data.getUserById;
            if (data) {
              this.user = { ...data };        // clone for editing
              this.originalUser = { ...data }; // backup original
              this.editMode = false;
            } else {
              this.user = null;
            }
          },
          error: () => {
            this.user = null;
          }
        });
    }
  }

  onEdit() {
    this.editMode = true;
  }

  onCancel() {
    this.editMode = false;
    if (this.originalUser) {
      this.user = { ...this.originalUser }; // restore exact original data
    }
  }

  onSave() {
    if (!this.user || !this.user.userId) return;

    const token = localStorage.getItem('authToken');

    const UPDATE_USER = gql`
    mutation UpdateUserById($userId: ID!, $userProfileDTO: UserProfileInput!) {
      updateUserById(userId: $userId, userProfileDTO: $userProfileDTO) {
        email
        firstName
        lastName
        aadharNumber
        city
        phoneNumber
      }
    }
  `;

    const variables = {
      userId: this.user.userId,
      userProfileDTO: {
        email: this.user.email,
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        phoneNumber: this.user.phoneNumber,
        city: this.user.city,
        aadharNumber: this.user.aadharNumber
      }
    };

    this.apollo.mutate({
      mutation: UPDATE_USER,
      variables,
      context: {
        headers: new HttpHeaders({
          Authorization: `Bearer ${token}`
        })
      }
    }).subscribe({
      next: (result: any) => {
        console.log('User updated successfully', result.data.updateUserById);
        this.editMode = false;
        this.originalUser = { ...this.user }; // update backup after save
        this.userService.notifyUsersChanged();
      },
      error: (err) => {
        console.error('Error updating user:', err);
      }
    });
  }

  onDelete() {
    if (!this.user || !this.user.userId) return;

    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      const token = localStorage.getItem('authToken');

      const DELETE_USER = gql`
      mutation DeleteUserById($userId: ID!) {
        deleteUserById(userId: $userId)
      }
    `;

      this.apollo.mutate<{ deleteUserById: string }>({
        mutation: DELETE_USER,
        variables: { userId: this.user.userId },
        context: {
          headers: new HttpHeaders({ Authorization: `Bearer ${token}` })
        }
      }).subscribe({
        next: (result) => {
          alert(result.data?.deleteUserById || 'User deleted successfully.');

          // Reset local component state
          this.user = null;
          this.originalUser = null;
          this.searchUserId = null;
          this.searchClicked = false;
          this.editMode = false;

          // Notify other components if using a service
          this.userService.notifyUsersChanged();
          this.bookingService.notifyBookingsChanged();
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          alert('Failed to delete user. Please try again.');
        }
      });
    }
  }
}
