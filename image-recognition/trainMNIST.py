import torch
from torch.utils.data import DataLoader
from torchvision import transforms
from torchvision.datasets import MNIST
import matplotlib.pyplot as plt
from tqdm import tqdm


class Net(torch.nn.Module):
    def __init__(self):
        super().__init__()
        self.conv1 = torch.nn.Conv2d(1, 32, kernel_size=3)
        self.conv2 = torch.nn.Conv2d(32, 64, kernel_size=3)
        self.fc1 = torch.nn.Linear(64*24*24, 128)
        self.fc2 = torch.nn.Linear(128, 10)

    def forward(self, x):
        x = torch.nn.functional.relu(self.conv1(x))
        x = torch.nn.functional.relu(self.conv2(x))
        x = x.view(-1, 64*24*24)
        x = torch.nn.functional.relu(self.fc1(x))
        x = torch.nn.functional.log_softmax(self.fc2(x), dim=1)
        return x


def get_data_loader(is_train):
    to_tensor = transforms.Compose([transforms.ToTensor()])
    data_set = MNIST("", is_train, transform=to_tensor, download=True)
    return DataLoader(data_set, batch_size=15, shuffle=True)


def evaluate(test_data, net):
    n_correct = 0
    n_total = 0
    with torch.no_grad():
        for (x, y) in tqdm(test_data, desc="Evaluating", leave=False):
            outputs = net.forward(x)
            for i, output in enumerate(outputs):
                if torch.argmax(output) == y[i]:
                    n_correct += 1
                n_total += 1
    return n_correct / n_total

def main():
    train_data = get_data_loader(is_train=True)
    test_data = get_data_loader(is_train=False)
    net = Net()

    print("Initial accuracy:", evaluate(test_data, net))

    optimizer = torch.optim.Adam(net.parameters(), lr=0.001)

    for epoch in range(2):
        train_loader = tqdm(train_data, desc=f"Epoch {epoch+1}/{5}", unit="batch")
        for (x, y) in train_loader:
            net.zero_grad()
            output = net.forward(x)
            loss = torch.nn.functional.nll_loss(output, y)
            loss.backward()
            optimizer.step()
            train_loader.set_postfix(loss=loss.item())

        print("Epoch", epoch + 1, "accuracy:", evaluate(test_data, net))

    misclassified = []
    with torch.no_grad():
        for (x, y) in test_data:
            outputs = net.forward(x)
            for i in range(len(outputs)):
                predicted_label = torch.argmax(outputs[i])
                if predicted_label != y[i]:
                    misclassified.append((x[i], y[i], predicted_label))
                if len(misclassified) >= 3:
                    break
            if len(misclassified) >= 3:
                break


    for n, (img, true_label, predicted_label) in enumerate(misclassified):
        plt.figure(n)
        plt.imshow(img.squeeze(), cmap="gray")
        plt.title(f"True label: {int(true_label)}, Prediction: {int(predicted_label)}")
    plt.show()


if __name__ == "__main__":
    main()