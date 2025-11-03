import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-login',
  imports: [FormsModule, RouterModule, CommonModule],
  templateUrl: './user-login.component.html',
  styleUrl: './user-login.component.css'
})
export class UserLoginComponent {
  email = '';
  password = '';
  errorMessage = '';

  isLoading = false;

  constructor(private authService: AuthService, private router: Router) { }

  onSubmit() {
    this.isLoading = true;

    this.authService.login(this.email, this.password).subscribe({
      next: (data) => {
        this.isLoading = false;
        if (data.error) {
          console.log(data.error);
          alert(data.error);
        } else {
          console.log(data);
          this.router.navigate(['/home']);
        }
      },
      error: () => {
        this.isLoading = false;
        alert("Something went wrong");
      }
    });
  }
}
