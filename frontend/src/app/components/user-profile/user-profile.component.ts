import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Apollo } from 'apollo-angular';
import { gql } from '@apollo/client/core';
import { HttpHeaders } from '@angular/common/http';


@Component({
  selector: 'app-user-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent {
  firstName = '';
  lastName = '';
  initials = '';
  email = '';
  phoneNumber = '';
  city = '';
  aadharNumber = '';

  constructor(private apollo: Apollo, private router: Router) { }

  ngOnInit() {
    const token = localStorage.getItem('authToken');
    console.log(token)
    if (token) {
      this.apollo.query({
        query: gql`
        query GetCurrentUserProfile {
          getCurrentUserProfile {
            email
            firstName
            lastName
            aadharNumber
            city
            phoneNumber
          }
        }
      `,
        context: {
          headers: new HttpHeaders({
            Authorization: `Bearer ${token}`
          })
        },
        fetchPolicy: 'no-cache'
      }).subscribe({
        next: (result: any) => {
          console.log(result);
          const profile = result.data.getCurrentUserProfile;
          this.firstName = profile.firstName;
          this.lastName = profile.lastName;
          this.initials = this.getInitials(this.firstName, this.lastName);
          this.email = profile.email;
          this.phoneNumber = profile.phoneNumber;
          this.city = profile.city;
          this.aadharNumber = profile.aadharNumber;
        },
        error: (err) => {
          console.error('Error fetching profile:', err);
        }
      });
    }
  }

  getInitials(first: string, last: string) {
    return (first.charAt(0) + last.charAt(0)).toUpperCase();
  }

  editMode = false;
  private originalPhoneNumber = this.phoneNumber;
  private originalCity = this.city;

  onEdit() {
    this.editMode = true;
    this.originalPhoneNumber = this.phoneNumber;
    this.originalCity = this.city;
  }

  onCancel() {
    this.editMode = false;
    this.phoneNumber = this.originalPhoneNumber;
    this.city = this.originalCity;
  }

  onSave() {
    const token = localStorage.getItem('authToken');

    if (token) {
      const UPDATE_CURRENT_USER_PROFILE = gql`
      mutation UpdateCurrentUserProfile($email: String!, $input: UpdateCurrentProfileInput!) {
        updateCurrentUserProfile(email: $email, input: $input) {
          profileId
          userId
          firstName
          lastName
          aadharNumber
          city
          phoneNumber
        }
      }
    `;

      this.apollo.mutate({
        mutation: UPDATE_CURRENT_USER_PROFILE,
        variables: {
          email: this.email,
          input: {
            city: this.city,
            phoneNumber: this.phoneNumber
          }
        },
        context: {
          headers: new HttpHeaders({
            Authorization: `Bearer ${token}`
          })
        }
      }).subscribe({
        next: (res: any) => {
          const updated = res.data.updateCurrentUserProfile;
          this.city = updated.city;
          this.phoneNumber = updated.phoneNumber;

          alert('Profile updated successfully.');
          this.editMode = false;
        },
        error: (err) => {
          console.error('Error updating profile:', err);
          alert('Failed to update profile. Please try again.');
        }
      });
    }
  }



  onDelete() {
    if (confirm('Are you sure you want to delete your profile? This action cannot be undone.')) {
      const token = localStorage.getItem('authToken');

      if (token) {
        const DELETE_USER_BY_EMAIL = gql`
        mutation DeleteUserByEmail($email: String!) {
          deleteUserByEmail(email: $email)
        }
      `;

        this.apollo.mutate({
          mutation: DELETE_USER_BY_EMAIL,
          variables: {
            email: this.email
          },
          context: {
            headers: new HttpHeaders({
              Authorization: `Bearer ${token}`
            })
          }
        }).subscribe({
          next: (res: any) => {
            console.log('User deleted:', res);
            alert('Your profile has been deleted successfully.');
            localStorage.removeItem('authToken');
            this.router.navigate(['/']);
          },
          error: (err) => {
            console.error('Error deleting user:', err);
            alert('Failed to delete profile. Please try again.');
          }
        });
      }
    }
  }
}
