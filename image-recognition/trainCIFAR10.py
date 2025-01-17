import torch
from torch.utils.data import DataLoader
from torchvision import transforms
from torchvision.datasets import CIFAR10
import matplotlib.pyplot as plt
from tqdm import tqdm


class Net(torch.nn.Module):
    def __init__(self):
        super().__init__()
        # 定义卷积层
        self.conv1 = torch.nn.Conv2d(3, 32, kernel_size=3)  # 输入为 3 通道（RGB），输出 32 通道
        self.conv2 = torch.nn.Conv2d(32, 64, kernel_size=3)

        # 使用示例输入确定展平后的大小
        with torch.no_grad():
            sample_input = torch.zeros(1, 3, 32, 32)  # 创建一个 32x32 的 CIFAR-10 样本
            sample_output = self.conv2(self.conv1(sample_input))
            self.flat_size = sample_output.view(-1).size(0)  # 展平后的大小

        # 定义全连接层
        self.fc1 = torch.nn.Linear(self.flat_size, 128)
        self.fc2 = torch.nn.Linear(128, 10)

    def forward(self, x):
        x = torch.nn.functional.relu(self.conv1(x))
        x = torch.nn.functional.relu(self.conv2(x))
        x = x.view(x.size(0), -1)  # 将特征展平
        x = torch.nn.functional.relu(self.fc1(x))
        x = torch.nn.functional.log_softmax(self.fc2(x), dim=1)
        return x


def get_data_loader(is_train):
    # 定义数据存储路径，比如放在当前项目的 'data' 文件夹下
    data_path = "./CIFARDATA"

    # CIFAR-10 数据集需要归一化
    transform = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))  # CIFAR-10 数据集的均值和标准差
    ])

    data_set = CIFAR10(data_path, train=is_train, transform=transform, download=True)
    return DataLoader(data_set, batch_size=15, shuffle=True)


def evaluate(test_data, net):
    n_correct = 0
    n_total = 0
    with torch.no_grad():
        for (x, y) in tqdm(test_data, desc="Evaluating", leave=False):
            outputs = net(x)
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
        train_loader = tqdm(train_data, desc=f"Epoch {epoch+1}/3", unit="batch")
        for (x, y) in train_loader:
            net.zero_grad()
            output = net(x)
            loss = torch.nn.functional.nll_loss(output, y)
            loss.backward()
            optimizer.step()
            train_loader.set_postfix(loss=loss.item())

        print("Epoch", epoch + 1, "accuracy:", evaluate(test_data, net))

    # 选择错误分类的图像
    misclassified = []
    with torch.no_grad():
        for (x, y) in test_data:
            outputs = net(x)
            for i in range(len(outputs)):
                predicted_label = torch.argmax(outputs[i])
                if predicted_label != y[i]:
                    misclassified.append((x[i], y[i], predicted_label))
                if len(misclassified) >= 3:
                    break
            if len(misclassified) >= 3:
                break

    # 显示前三个错误分类的图像
    for n, (img, true_label, predicted_label) in enumerate(misclassified):
        plt.figure(n)
        plt.imshow((img.permute(1, 2, 0) * 0.5 + 0.5).numpy())  # 反归一化并转换通道
        plt.title(f"True label: {int(true_label)}, Prediction: {int(predicted_label)}")
    plt.show()


if __name__ == "__main__":
    main()
